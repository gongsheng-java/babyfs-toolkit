package com.babyfs.tk.service.biz.list.impl;

import com.babyfs.tk.commons.base.Pair;
import com.babyfs.tk.commons.utils.ListUtil;
import com.babyfs.tk.commons.utils.ThreadUtil;
import com.babyfs.tk.service.basic.INameResourceService;
import com.babyfs.tk.service.basic.redis.IRedis;
import com.babyfs.tk.service.basic.redis.LuaScript;
import com.babyfs.tk.service.biz.base.entity.list.BaseStrListCounterEntity;
import com.babyfs.tk.service.biz.base.entity.list.BaseStrListEntity;
import com.babyfs.tk.service.biz.cache.BaseListCacheDataServiceImpl;
import com.babyfs.tk.service.biz.cache.CacheParameter;
import com.babyfs.tk.service.biz.cache.CacheUtils;
import com.babyfs.tk.service.biz.constants.Const;
import com.babyfs.tk.service.biz.counter.IWithRedisCounterService;
import com.babyfs.tk.service.biz.counter.impl.RedisCounterService;
import com.babyfs.tk.service.biz.list.dal.IBaseStrListCounterDao;
import com.babyfs.tk.service.biz.list.dal.IBaseStrListDao;
import com.babyfs.tk.service.biz.list.IStrListDataService;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 *
 */
public class BaseStrListDataServiceImpl<SL extends BaseStrListEntity, C extends BaseStrListCounterEntity> implements IStrListDataService<SL, C>, IWithRedisCounterService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseStrListDataServiceImpl.class);
    public static final int COUNTER_SYNC_SLOTS = 10;
    public static final int LIST_COUNTER_DEFAULT_TYPE = 2;

    private final INameResourceService<IRedis> cacheService;
    private final IBaseStrListDao<SL> dao;
    private final ListCacheImpl listCache;
    private final CacheParameter listOwnerTargetIdCacheParam;
    private final RedisCounterService redisCounterService;
    /**
     * 是否将targetId作为list cache的scoreId,如果为true,则将{@link BaseStrListEntity#getTargetId()}作为list cache的score id,否则将{@link BaseStrListEntity#getId()}
     * 作为list cache的score id
     */
    private final boolean targetAsScoreId;

    /**
     * 使用{@link SL#getTargetId()}作为list cache的score id,即{@link #targetAsScoreId}为true
     *
     * @param cacheService           cache servcie
     * @param listDao                listDao
     * @param counterDao             list counter dao
     * @param listCacheParameter     list cache parameter
     * @param counterCacheParameter  list counter cache parameter
     * @param listCounterEntityClass list counter 的实体类型
     * @param maxListCount           list cache最大个数
     */
    public BaseStrListDataServiceImpl(INameResourceService<IRedis> cacheService,
                                      IBaseStrListDao<SL> listDao,
                                      IBaseStrListCounterDao<C> counterDao,
                                      CacheParameter listCacheParameter,
                                      CacheParameter counterCacheParameter,
                                      Class<C> listCounterEntityClass,
                                      int maxListCount) {
        this(cacheService, listDao, counterDao, listCacheParameter, counterCacheParameter, listCounterEntityClass, maxListCount, true);
    }

    /**
     * @param cacheService           cache servcie
     * @param listDao                listDao
     * @param counterDao             list counter dao
     * @param listCacheParameter     list cache parameter
     * @param counterCacheParameter  list counter cache parameter
     * @param listCounterEntityClass list counter 的实体类型
     * @param maxListCount           list cache最大个数
     * @param targetAsScoreId        是否将targetId作为scoreId
     */
    public BaseStrListDataServiceImpl(INameResourceService<IRedis> cacheService,
                                      IBaseStrListDao<SL> listDao,
                                      IBaseStrListCounterDao<C> counterDao,
                                      CacheParameter listCacheParameter,
                                      CacheParameter counterCacheParameter,
                                      Class<C> listCounterEntityClass,
                                      int maxListCount,
                                      boolean targetAsScoreId) {
        this.cacheService = Preconditions.checkNotNull(cacheService);
        this.dao = Preconditions.checkNotNull(listDao);
        this.listCache = new ListCacheImpl(Preconditions.checkNotNull(cacheService), listCacheParameter, maxListCount);
        this.listOwnerTargetIdCacheParam = new CacheParameter(listCacheParameter.getRedisExpireSecond(), listCacheParameter.getRedisServiceGroup(), listCacheParameter.getRedisKeyPrefix() + "eow2i_", "");
        this.targetAsScoreId = targetAsScoreId;
        this.redisCounterService = new RedisCounterService(
                listCounterEntityClass.getName(),
                counterCacheParameter,
                new StrListCounterPersistService<>(counterDao),
                COUNTER_SYNC_SLOTS,
                this.cacheService);
    }

    @Override
    public SL add(SL entity) {
        Preconditions.checkNotNull(entity);
        Preconditions.checkNotNull(entity.getOwnerId());
        final String key = buildOwnerTargetKey(entity.getOwnerId(), entity.getTargetId());
        final String lockKey = "lock_" + key;
        boolean locked = false;
        try {
            //使用乐观锁,尝试避免重复添加
            locked = LuaScript.tryLock(lockKey, Const.DEFAULT_ADD_LOCK_SECONDS, this.listOwnerTargetIdCacheParam, this.cacheService);
            if (!locked) {
                LOGGER.warn("not lock {} with key {},skip add", key, lockKey);
                return null;
            }
            //增加实体
            SL save = this.dao.save(entity);
            if (save == null || save.getId() <= 0) {
                LOGGER.error("Add save {} fail", entity);
                return null;
            }
            //增加计数 +1
            redisCounterService.incr(LIST_COUNTER_DEFAULT_TYPE, save.getOwnerId(), ListCounterPersistService.buildCounterField(1));
            //增加list cache
            long scoreId = this.targetAsScoreId ? save.getTargetId() : save.getId();
            listCache.add(save.getOwnerId(), save.getTargetId(), scoreId);
            return save;
        } catch (Exception e) {
            LOGGER.error("Add " + entity + "fail", e);
            throw e;
        } finally {
            ThreadUtil.runQuitely(() -> CacheUtils.delete(key, this.listOwnerTargetIdCacheParam, this.cacheService));
            if (locked) {
                LuaScript.unLock(lockKey, this.listOwnerTargetIdCacheParam, this.cacheService);
            }
        }
    }


    @Override
    public boolean del(final String ownerId, final long targetId) {
        Preconditions.checkNotNull(ownerId);
        try {
            int deleted = dao.delete(ownerId, targetId);
            if (deleted <= 0) {
                return false;
            }
            //减少计数
            redisCounterService.incr(LIST_COUNTER_DEFAULT_TYPE, ownerId, ListCounterPersistService.buildCounterField(-deleted));
            //减少list cache
            listCache.delete(ownerId, targetId);
            return true;
        } catch (Exception e) {
            LOGGER.error("Del ownerId=" + ownerId + ",targetId=" + targetId + " fail", e);
            throw e;
        } finally {
            CacheUtils.delete(buildOwnerTargetKey(ownerId, targetId), this.listOwnerTargetIdCacheParam, this.cacheService);
        }
    }

    @Override
    public Long getOwnerTargetId(final String ownerId, final long targetId) {
        final String key = buildOwnerTargetKey(ownerId, targetId);
        Long id = CacheUtils.get(key, this.listOwnerTargetIdCacheParam, this.cacheService);
        if (id == null) {
            List<SL> list = dao.getByOwnerAndTargetId(ownerId, targetId);
            if (ListUtil.isNotEmtpy(list)) {
                id = list.get(0).getId();
                CacheUtils.set(key, id, this.listOwnerTargetIdCacheParam, this.cacheService);
            } else {
                //设置一个负值
                CacheUtils.set(key, -1L, this.listOwnerTargetIdCacheParam, this.cacheService);
            }
        }

        if (id != null && id > 0) {
            return id;
        } else {
            return null;
        }
    }

    @Override
    public long getCount(String ownerId) {
        long counterValue = ListCounterPersistService.getCounterValue(redisCounterService.get(LIST_COUNTER_DEFAULT_TYPE, String.valueOf(ownerId)));
        if (counterValue < 0) {
            //TODO fix counter 为负数的情况
            LOGGER.warn("invalid list.couner:{},try to fix", counterValue);
            counterValue = 0;
        }
        return counterValue;
    }

    @Override
    public Pair<Long, List<Long>> loadList(String ownerId, int page, int pageSize, long cursor) {
        return listCache.loadList(ownerId, page, pageSize, cursor);
    }

    @Override
    public Pair<Long, List<Pair<Long, Long>>> loadListWithScoreId(String ownerId, int page, int pageSize, long cursor) {
        return listCache.loadListWithScoreId(ownerId, page, pageSize, cursor);
    }

    @Override
    public Set<RedisCounterService> getRedisCounterService() {
        return Sets.newHashSet(redisCounterService);
    }

    private String buildOwnerTargetKey(String ownerId, long targetId) {
        return ownerId + "_" + targetId;
    }

    private class ListCacheImpl extends BaseListCacheDataServiceImpl<String> {
        ListCacheImpl(INameResourceService<IRedis> cacheService, CacheParameter listCacheParameter, int maxListCount) {
            super(cacheService, listCacheParameter, maxListCount);
        }

        @Override
        protected long loadCount(String listId) {
            return BaseStrListDataServiceImpl.this.getCount(listId);
        }

        @Override
        protected List<Pair<Long, Long>> loadIdsByCursor(String listId, long cursor, long pageSize) {
            final List<Object[]> objects;
            if (targetAsScoreId) {
                objects = dao.getByNextCursorByTargetId(listId, pageSize, cursor);
            } else {
                objects = dao.getByNextCursorById(listId, pageSize, cursor);
            }

            return parseListResult(objects);
        }

        @Override
        protected List<Pair<Long, Long>> loadIdsByPage(String listId, long page, long pageSize) {
            final List<Object[]> objects;
            if (targetAsScoreId) {
                objects = dao.getByPageByTargetId(listId, (page - 1) * pageSize, pageSize);
            } else {
                objects = dao.getByPageById(listId, (page - 1) * pageSize, pageSize);
            }
            return parseListResult(objects);
        }

        private List<Pair<Long, Long>> parseListResult(List<Object[]> objects) {
            return ListDataServiceSupport.parseListResult(targetAsScoreId, objects);
        }
    }
}

package com.babyfs.tk.service.biz.cache;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.babyfs.tk.commons.base.Pair;
import com.babyfs.tk.dal.orm.IEntity;
import com.babyfs.tk.dal.db.DaoFactory;
import com.babyfs.tk.dal.db.IDao;
import com.babyfs.tk.service.basic.INameResourceService;
import com.babyfs.tk.service.basic.guice.annotation.ServiceRedis;
import com.babyfs.tk.service.basic.redis.IRedis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.transaction.TransactionStatus;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 基于Redis提供的缓存基础服务 , 需要使用的数据服务可以继承该类  TODO 暂时不做为服务抽象出来 (如支持Memcached)
 * 提供 Cache + Dao 的封装方法
 * 同时处理缓存和数据库
 * <p/>
 * 1.针对某个Entity进行全面缓存（CRUD） , 仅限 IEntity
 * 2.执行其他SQL更新Entity，清除缓存
 */
public class BaseCacheDataService implements IBaseCacheDataService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseCacheDataService.class);

    static final Function<Object[],Long> ObjArray2IdFunction = new Function<Object[], Long>() {
        @Override
        public Long apply(Object[] input) {
            Preconditions.checkArgument(input != null);
            return Long.parseLong(input[0].toString());
        }
    };

    @Inject
    @ServiceRedis
    protected INameResourceService<IRedis> redisService;

    private final ThreadLocal<List<Pair<Long, CacheParameter>>> transCacheCleanList = new ThreadLocal<List<Pair<Long, CacheParameter>>>() {
        @Override
        public List<Pair<Long, CacheParameter>> get() {
            return Lists.newLinkedList();
        }
    };

    private final ThreadLocal<Boolean> callInTrans = new ThreadLocal<Boolean>() {
        @Override
        public Boolean get() {
            return false;
        }
    };

    /**
     * 新增 Entity 对象
     *
     * @param entity
     * @param dao
     * @param <T>
     * @return
     */
    public <T extends IEntity> T addEntity(T entity, IDao<T> dao) {
        Preconditions.checkArgument(null != entity, "entity is null");
        Preconditions.checkArgument(null != dao, "dao is null");
        return (T) dao.save(entity);
    }

    /**
     * 根据id取得Entity对象,直接从数据库中加载
     *
     * @param entityClass
     * @param id
     * @param dao
     * @param <T>
     * @return
     */
    public <T extends IEntity> T getEntity(Class<T> entityClass, long id, IDao<T> dao) {
        return dao.get(id, entityClass);
    }

    /**
     * 更新对象
     *
     * @param entity
     * @param dao
     * @param <T>
     * @return
     */
    public <T extends IEntity> boolean updateEntity(T entity, IDao<T> dao) {
        return dao.update(entity);
    }

    /**
     * 根据读取的id列表，获取第一个Entity对象
     *
     * @param results
     * @return
     */
    public <T extends IEntity> T getFirstEntity(List<Object[]> results, Class<T> entityClass, IDao<T> dao, CacheParameter cacheParameter) {
        if (results != null && !results.isEmpty()) {
            Object[] seqObjArr = results.get(0);
            if (seqObjArr != null) {
                long id = Long.parseLong(seqObjArr[0].toString());
                return this.getEntityWithCache(entityClass, id, dao, cacheParameter);
            }
        }
        return null;
    }


    /**
     * 根据 id 取得 Entity 对象, 先从Cache中查询
     *
     * @param id
     * @param dao
     * @param entityClass
     * @param cacheParameter
     * @param <T>
     * @return
     */
    public <T extends IEntity> T getEntityWithCache(Class<T> entityClass, long id, IDao<T> dao, CacheParameter cacheParameter) {
        if (id <= 0) {
            return null;
        }
        Preconditions.checkArgument(null != dao, "dao is null.");
        Preconditions.checkArgument(null != entityClass, "entityClass is null.");
        Preconditions.checkArgument(null != cacheParameter, "cacheParameter is null.");
        T entity = CacheUtils.get(id, cacheParameter, redisService);
        if (entity == null) {
            entity = getEntity(entityClass, id, dao);
            if (entity != null && entity.getId() > 0) {
                CacheUtils.set(id, entity, cacheParameter, redisService);
            }
        }
        return entity;
    }

    /**
     * 根据 Id 数组取得 Entity 对象列表, 先从 Cache 中查询, 如果 Cache 中没有数据, 则从给定的 getEntityFunc 对象中查询.
     * 注意: 该函数只对 Cache 进行读操作, 不会进行 Cache 的写操作!
     * 如果有写 Cache 的需求, 应该交由 getEntityFunc 对象来做, 也就是将 Cache 的写操作交给业务代码来决定...
     *
     * @param entityClass    实体类
     * @param idArray        实体 Id 数组
     * @param cacheParameter 缓存参数
     * @param getEntityFunc  获取实体的函数
     * @param <T>
     * @return 注意:返回的列表元素中可能含有null,即:如果对应id找不到entity,对应序列的元素为null
     */
    @Override
    public <T extends IEntity> List<T> getEntityListWithCache(Class<T> entityClass, long[] idArray, CacheParameter cacheParameter, Function<Long, T> getEntityFunc) {
        Preconditions.checkArgument(null != entityClass, "entityClass is null.");
        Preconditions.checkArgument(null != idArray, "idArray is null.");
        Preconditions.checkArgument(null != cacheParameter, "cacheParameter is null.");

        String[] keyArray = new String[idArray.length];
        for (int i = 0; i < keyArray.length; i++) {
            if (idArray[i] > 0) {
                keyArray[i] = cacheParameter.getCacheKey(idArray[i]);
            }
        }

        try {
            // 从 Redis 里批量获取实体列表
            IRedis redis = this.redisService.get(cacheParameter.getRedisServiceGroup());
            List<T> entityList = redis.getObjectList(keyArray);

            if (entityList.size() == idArray.length) {
                // 如果 Redis 返回的是完整的列表, 就直接返回!
                return entityList;
            }

            // 如果 Redis 返回的列表不完整,
            // 那么这时候就需要从数据库中把数据读出来...
            Map<Long, T> entityMap = Maps.newHashMapWithExpectedSize(entityList.size());
            for (T entity : entityList) {
                entityMap.put(entity.getId(), entity);
            }

            List<T> rtnList = Lists.newArrayListWithExpectedSize(idArray.length);
            for (long id : idArray) {
                T entity = entityMap.get(id);
                if (entity != null && entity.getId() > 0) {
                    rtnList.add(entity);
                    continue;
                }
                if (null != getEntityFunc) {
                    // 从给定的方法中获取数据
                    entity = getEntityFunc.apply(id);
                }
                if (entity != null && entity.getId() > 0) {
                    rtnList.add(entity);
                    continue;
                }
                // 确保入参idArray总数和序列跟返回的List一致,如果对应id找不到entity,对应序列的元素为null
                rtnList.add(null);
            }

            return rtnList;

        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return Collections.emptyList();
        }
    }

    /**
     * 更新 Entity 整个对象，更新 DB & Cache
     *
     * @param entity
     * @param dao
     * @param cacheParameter
     * @param <T>
     * @return
     */
    public <T extends IEntity> boolean updateEntityWithCache(T entity, IDao<T> dao, CacheParameter cacheParameter) {
        Preconditions.checkArgument(null != entity, "entity is null");
        Preconditions.checkArgument(null != dao, "dao is null");
        Preconditions.checkArgument(null != cacheParameter, "cacheParameter is null");
        addCacheToCleanList(entity.getId(), cacheParameter);
        boolean isSuccess = updateEntity(entity, dao);
        CacheUtils.delete(entity.getId(), cacheParameter, redisService);
        return isSuccess;
    }

    /**
     * 标准删除 Entity 对象, 从 DB & Cache
     *
     * @param entity
     * @param dao
     * @param cacheParameter
     * @param <T>
     * @return
     */
    public <T extends IEntity> boolean delEntityWithCache(T entity, IDao<T> dao, CacheParameter cacheParameter) {
        Preconditions.checkArgument(null != entity, "entity is null");
        Preconditions.checkArgument(null != dao, "dao is null");
        Preconditions.checkArgument(null != cacheParameter, "cacheParameter is null");
        addCacheToCleanList(entity.getId(), cacheParameter);
        boolean isSuccess = dao.delete(entity);
        CacheUtils.delete(entity.getId(), cacheParameter, redisService);
        return isSuccess;
    }

    /**
     * 通过 id 删除 Entity 对象 , 从 DB & Cache
     *
     * @param entityClass
     * @param id
     * @param dao
     * @param cacheParameter
     * @param <T>
     * @return
     */
    public <T extends IEntity> boolean delEntityByIdWithCache(Class<T> entityClass, long id, IDao<T> dao, CacheParameter cacheParameter) {
        Preconditions.checkArgument(null != entityClass, "entityClass is null");
        Preconditions.checkArgument(null != dao, "dao is null");
        Preconditions.checkArgument(null != cacheParameter, "cacheParameter is null");
        addCacheToCleanList(id, cacheParameter);
        T entity = getEntity(entityClass, id, dao);
        if (entity != null) {
            return this.delEntityWithCache(entity, dao, cacheParameter);
        }
        return false;

    }

    /**
     * 根据一组id查询 Entity 实体列表 ： 不支持分页，不从数据库批量读取
     *
     * @param ids
     * @param entityClass
     * @param dao
     * @param cacheParameter
     * @param <T>
     * @return
     */
    public <T extends IEntity> List<T> queryEntitiesWithCache(List<Long> ids, Class<T> entityClass, IDao<T> dao, CacheParameter cacheParameter) {
        List<T> results = Lists.newArrayList();
        for (long id : ids) {
            T result = this.getEntityWithCache(entityClass, id, dao, cacheParameter);
            if (result != null) {
                results.add(result);
            }
        }
        return results;
    }


    public <T extends IEntity> List<T> queryEntitiesByObjIdLambda(List<Object[]> results, Class<T> clazz, IDao<T> dao, CacheParameter param) {
        return results.stream().map(arg -> this.getEntityWithCache(
                clazz, Long.parseLong(arg[0].toString()), dao, param))
                .collect(Collectors.toList());
    }


    /**
     * 通过查询 QUERY_COLUMNS 查出的 id 结果 List<Object[]> , 查询 Entity 列表
     *
     * @param results
     * @param tClass
     * @param dao
     * @param cacheParameter
     * @param <T>
     * @return
     */
    public <T extends IEntity> List<T> queryEntitiesByObjectId(List<Object[]> results, Class<T> tClass, IDao<T> dao, CacheParameter cacheParameter) {
        if (results == null || results.isEmpty()) {
            return Lists.newArrayListWithCapacity(0);
        } else {
            List<Long> ids = Lists.transform(results, ObjArray2IdFunction);
            return this.queryEntitiesWithCache(ids, tClass, dao, cacheParameter);
        }
    }

    /**
     * 删除缓存 ：用于非整体Entity更新后的缓存清除， 如使用SQL更新一个字段的时候, 软删除的时候等
     *
     * @param id
     * @param cacheParameter
     * @param <T>
     */
    public <T extends IEntity> void clearEntityCache(boolean isInTransaction, long id, CacheParameter cacheParameter) {
        if (isInTransaction) {
            addCacheToCleanList(id, cacheParameter);
        } else {
            CacheUtils.delete(id, cacheParameter, redisService);
        }
    }

    /**
     * 执行事务操作，完成事务后清理Cache
     *
     * @param daoFactory
     * @param entityClass
     * @param shardValueMap
     * @param func
     * @param <T>
     * @param <E>
     */
    @Override
    public <T, E extends IEntity> T doTransactionWithCleanCache(DaoFactory daoFactory, Class<E> entityClass, Map<String, Object> shardValueMap, Function<Pair<NamedParameterJdbcOperations, TransactionStatus>, T> func) {
        List<Pair<Long, CacheParameter>> list = transCacheCleanList.get();
        boolean initInTrans = callInTrans.get();
        if (!initInTrans) {
            callInTrans.set(true);
        }
        try {
            return daoFactory.getDaoSupport().doTransaction(entityClass, shardValueMap, func);
        } finally {
            for (Pair<Long, CacheParameter> pair : list) {
                try {
                    CacheUtils.delete(pair.first, pair.second, redisService);
                } catch (Exception e) {
                    LOGGER.error("Del cache" + pair + " error.", e);
                }
            }
            list.clear();
            if (!initInTrans) {
                callInTrans.set(false);
            }
        }
    }

    public void addCacheToCleanList(long id, CacheParameter cacheParameter) {
        Preconditions.checkNotNull(cacheParameter);
        if (!callInTrans.get()) {
            return;
        }
        List<Pair<Long, CacheParameter>> list = transCacheCleanList.get();
        list.add(Pair.of(id, cacheParameter));
    }
}

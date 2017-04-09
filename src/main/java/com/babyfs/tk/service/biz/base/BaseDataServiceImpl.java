package com.babyfs.tk.service.biz.base;

import com.babyfs.tk.service.biz.base.entity.IBaseEntity;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.babyfs.tk.commons.base.Pair;
import com.babyfs.tk.commons.enums.DeleteEnum;
import com.babyfs.tk.commons.utils.ListUtil;
import com.babyfs.tk.dal.db.DaoFactory;
import com.babyfs.tk.service.biz.cache.BaseCacheDataService;
import com.babyfs.tk.service.biz.cache.CacheParameter;
import com.babyfs.tk.service.biz.cache.CacheUtils;
import com.babyfs.tk.dal.DalUtil;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.transaction.TransactionStatus;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @param <T>
 */
public abstract class BaseDataServiceImpl<T extends IBaseEntity> extends BaseCacheDataService implements IDataService<T> {
    private final IBaseDao<T> dao;
    protected final Class<T> clazz;
    protected final CacheParameter cacheParam;
    protected final DaoFactory daoFactory;

    /**
     * @param clazz      实体的类型,非空
     * @param dao        dao
     * @param cacheParam cache parameter
     */
    protected BaseDataServiceImpl(Class<T> clazz, IBaseDao<T> dao, DaoFactory daoFactory, CacheParameter cacheParam) {
        this.clazz = Preconditions.checkNotNull(clazz);
        this.dao = Preconditions.checkNotNull(dao);
        this.cacheParam = Preconditions.checkNotNull(cacheParam);
        this.daoFactory = Preconditions.checkNotNull(daoFactory);
    }

    @Override
    public T add(T entity) {
        if (entity.getCt() <= 0) {
            entity.setCt(System.currentTimeMillis());
        }
        return super.addEntity(entity, dao);
    }

    @Override
    public boolean update(T entity) {
        entity.setUt(System.currentTimeMillis());
        return super.updateEntityWithCache(entity, dao, cacheParam);
    }

    @Override
    public T get(long id) {
        return super.getEntityWithCache(clazz, id, dao, cacheParam);
    }

    @Override
    public boolean del(long id) {
        return super.delEntityByIdWithCache(clazz, id, dao, cacheParam);
    }

    @Override
    public boolean del(T entity) {
        return super.delEntityWithCache(entity, dao, cacheParam);
    }

    /**
     * 软删除,设置删除标志为{@link DeleteEnum#DELETED}
     *
     * @param id
     * @return 如果更新失败, 返回false;否则返回true
     */
    @Override
    public boolean softDel(long id) {
        return softDel(this.get(id));
    }

    /**
     * 软删除,设置删除标志为{@link DeleteEnum#DELETED}
     *
     * @param entity entity
     * @return 如果entity为空或者更新失败, 返回false;否则返回true
     */
    @Override
    public boolean softDel(T entity) {
        if (entity == null) {
            return false;
        }
        entity.setDel(DeleteEnum.DELETED.getVal());
        return updateWithVersion(entity);
    }

    @Override
    public boolean cancelSoftDel(long id) {
        return cancelSoftDel(this.get(id));
    }

    @Override
    public boolean cancelSoftDel(T entity) {
        if (entity == null) {
            return false;
        }
        entity.setDel(DeleteEnum.NORMAL.getVal());
        return updateWithVersion(entity);
    }

    @Override
    public boolean updateWithVersion(T entity) {
        entity.setUt(System.currentTimeMillis());
        final long version = entity.getVer();
        final long newVersion = version + 1;
        boolean isSuccess = false;
        try {
            addCacheToCleanList(entity.getId(), cacheParam);
            entity.setVer(newVersion);
            isSuccess = dao.updateWithVersion(entity, version);
            CacheUtils.delete(entity.getId(), cacheParam, redisService);
            return isSuccess;
        } finally {
            //如果更新不成功,重置版本号
            if (!isSuccess) {
                entity.setVer(version);
            }
        }
    }

    @Override
    public List<T> getEntityListWithCache(long[] ids) {
        return getEntityListWithCache(ids, this::get);
    }

    @Override
    public List<T> getEntityListWithCache(long[] ids, Function<Long, T> getEntityFunc) {
        return getEntityListWithCache(this.clazz, ids, this.cacheParam, getEntityFunc);
    }

    @Override
    public <T> T doTransactionWithCleanCache(Function<Pair<NamedParameterJdbcOperations, TransactionStatus>, T> func) {
        // 执行事务过程
        return super.doTransactionWithCleanCache(
                this.daoFactory,
                this.clazz,
                Collections.emptyMap(),
                func
        );
    }

    @Override
    public List<T> scrollFetch(long scrollId, int size) {
        Preconditions.checkArgument(scrollId >= 0, "scrollId >=0");
        Preconditions.checkArgument(size > 0, "size >=0");

        String condition = " WHERE id >= :scrollId ORDER BY id ASC LIMIT :from,:size";
        MapSqlParameterSource queryParams = new MapSqlParameterSource();
        queryParams.addValue("scrollId", scrollId);
        queryParams.addValue("from", 0);
        queryParams.addValue("size", size);

        List<Object[]> idColumns = daoFactory.getDaoSupport().queryEntityColumns(this.clazz, "id", condition, queryParams, Collections.emptyMap());

        List<Long> ids = DalUtil.extractColumn(idColumns, 0);
        if (ListUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }

        //不从cache加载,避免大量占用cache内存
        return ids.stream().map(id -> this.getEntity(this.clazz, id, dao)).filter(entity -> entity != null).collect(Collectors.toList());
    }
}

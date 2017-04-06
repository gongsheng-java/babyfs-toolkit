package com.babyfs.tk.service.biz.cache;

import com.google.common.base.Function;
import com.babyfs.tk.commons.base.Pair;
import com.babyfs.tk.orm.IEntity;
import com.babyfs.tk.dal.db.DaoFactory;
import com.babyfs.tk.service.biz.cache.utils.CacheParameter;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.transaction.TransactionStatus;

import java.util.List;
import java.util.Map;

/**
 *
 */
public interface IBaseCacheDataService {
    /**
     * 执行事务操作，完成事务后清理Cache
     *
     * @param daoFactory
     * @param entityClass
     * @param shardValueMap
     * @param func
     * @param <T>
     * @param <E>
     * @return
     */
    <T, E extends IEntity> T doTransactionWithCleanCache(DaoFactory daoFactory, Class<E> entityClass, Map<String, Object> shardValueMap, Function<Pair<NamedParameterJdbcOperations, TransactionStatus>, T> func);

    /**
     * @param entityClass
     * @param idArray
     * @param cacheParameter
     * @param getEntityFunc
     * @param <T>
     * @return
     */
    <T extends IEntity> List<T> getEntityListWithCache(Class<T> entityClass, long[] idArray, CacheParameter cacheParameter, Function<Long, T> getEntityFunc);
}

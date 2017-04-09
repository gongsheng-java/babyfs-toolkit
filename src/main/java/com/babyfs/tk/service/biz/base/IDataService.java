package com.babyfs.tk.service.biz.base;

import com.babyfs.tk.service.biz.base.entity.IBaseEntity;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.babyfs.tk.commons.base.Pair;
import com.babyfs.tk.commons.enums.DeleteEnum;
import com.babyfs.tk.service.basic.INameResourceService;
import com.babyfs.tk.service.basic.redis.IRedis;
import com.babyfs.tk.service.biz.cache.CacheParameter;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.transaction.TransactionStatus;

import java.util.List;


/**
 * {@link IBaseEntity}实体的基础的数据服务接口
 */
public interface IDataService<T extends IBaseEntity> {
    /**
     * 增加一个实体
     *
     * @param entity entity
     * @return
     */
    T add(T entity);

    /**
     * 更新一个实体
     *
     * @param entity entity
     * @return
     */
    boolean update(T entity);

    /**
     * 根据ID取得实体
     *
     * @param id id
     * @return
     */
    T get(long id);

    /**
     * 删除一个实体
     *
     * @param id id
     * @return
     */
    boolean del(long id);

    /**
     * 删除一个实体
     *
     * @param entity entity
     * @return
     */
    boolean del(T entity);

    /**
     * 软删除
     *
     * @param id id
     * @return 如果更新失败, 返回false;否则返回true
     */
    boolean softDel(long id);

    /**
     * 取消软删除
     *
     * @param id id
     * @return 如果更新失败, 返回false;否则返回true
     */
    boolean cancelSoftDel(long id);

    /**
     * 软删除
     *
     * @param entity entity
     * @return 如果entity为空或者更新失败, 返回false;否则返回true
     */
    boolean softDel(T entity);

    /**
     * 取消软删除
     *
     * @param entity entity
     * @return 如果entity为空或者更新失败, 返回false;否则返回true
     */
    boolean cancelSoftDel(T entity);

    /**
     * 更新实体,并递增版本号{@link IBaseEntity#getVer()},更新时会检查数据库中的版本号是否与entity版本号是否匹配.
     * <p>
     * 如果更新失败,则将{@link IBaseEntity#getVer()}重置为之前的版本号
     *
     * @param entity entity
     * @return true, 更新成功;false,更新失败
     */
    boolean updateWithVersion(T entity);


    /**
     * 根据唯一key查找id,然后根据id再加载实体
     *
     * @param key               key,非空
     * @param keyCacheParameter key和id对应的缓存配置,非空
     * @param redisService      redis缓存服务,非空
     * @param keyLoader         根据key加载实体的加载器,非空
     * @param rechecker         验证key是否与实体匹配的验证器,可以为空
     * @return 操作结果
     */
    default T getByUniqKeyAndThenById(String key,
                                      CacheParameter keyCacheParameter,
                                      INameResourceService<IRedis> redisService,
                                      Function<String, T> keyLoader,
                                      Function<Pair<String, T>, Boolean> rechecker) {

        return DataServiceUtil.getByUniqKeyAndThenById(key, keyCacheParameter, redisService, keyLoader, rechecker, this::get);
    }

    /**
     * 根据唯一key查找id,然后根据id再加载实体,如果key不存在,则构建一个null protect key,将null prtect key设置为-1,有效期为个1分钟
     * <p>
     * <b>调用这个方法,需要注意及时调用{@link #deleteNullKey(String, CacheParameter, INameResourceService)}删除null key</b>
     *
     * @param key                     key,非空
     * @param keyCacheParameter       key和id对应的缓存配置,非空
     * @param redisService            redis缓存服务,非空
     * @param keyLoader               根据key加载实体的加载器,非空
     * @param rechecker               验证key是否与实体匹配的验证器,可以为空
     * @param nullProtect             是否需要null保护
     * @param nullProtectExpireSecond 空保护的有效期,单位秒
     * @return 操作结果
     */
    default T getByUniqKeyThenIdWithNullProtect(final String key,
                                                final CacheParameter keyCacheParameter,
                                                final INameResourceService<IRedis> redisService,
                                                final Function<String, T> keyLoader,
                                                final Function<Pair<String, T>, Boolean> rechecker,
                                                boolean nullProtect,
                                                int nullProtectExpireSecond) {
        return DataServiceUtil.getByUniqKeyThenIdWithNullProtect(key, keyCacheParameter, redisService, keyLoader, rechecker, this::get, nullProtect, nullProtectExpireSecond);
    }

    /**
     * 删除null key
     *
     * @param key
     * @param keyCacheParameter
     * @param redisService
     * @see DataServiceUtil#deleteNullKey(String, CacheParameter, INameResourceService)
     */
    default void deleteNullKey(final String key,
                               final CacheParameter keyCacheParameter,
                               final INameResourceService<IRedis> redisService) {
        DataServiceUtil.deleteNullKey(key, keyCacheParameter, redisService);
    }

    /**
     * 迭代判断入参{@code ids}数组中的所有id,是否在当前实体表中都存在并有效
     * <p>
     * 判断条件:
     * <li>每个id都>0;
     * <li>每个id都对应一个实体
     * <li>每个id对应的实体状态都是:未删除 {@link DeleteEnum#NORMAL}
     *
     * @param ids 需要被检查的id数组
     * @return 如果检查都通过返回true, 只要有任何一个不通过返回false
     */
    default boolean checkIdsAllOk(long... ids) {
        Preconditions.checkNotNull(ids);
        Preconditions.checkArgument(ids.length > 0);
        for (long id : ids) {
            if (id <= 0) return false;
            T entity = this.get(id);
            if (null == entity) return false;
            if (DeleteEnum.DELETED.getVal() == entity.getDel()) return false;
        }
        return true;
    }

    /**
     * 批量获取实体
     *
     * @param ids not null
     * @return
     */
    List<T> getEntityListWithCache(long[] ids);

    /**
     * 批量获取实体
     *
     * @param ids           not null
     * @param getEntityFunc not null
     * @return
     */
    List<T> getEntityListWithCache(long[] ids, Function<Long, T> getEntityFunc);

    /**
     * 执行事务过程
     *
     * @param func
     * @param <T>
     * @return
     */
    <T> T doTransactionWithCleanCache(Function<Pair<NamedParameterJdbcOperations, TransactionStatus>, T> func);

    /**
     * 根据id + 游标 id查询实体
     *
     * @param scrollId 游标id,>=0
     * @param size     长度,>0
     * @return
     */
    List<T> scrollFetch(long scrollId, int size);
}
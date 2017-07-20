package com.babyfs.tk.service.biz.cache;

import com.babyfs.tk.service.basic.INameResourceService;
import com.babyfs.tk.service.basic.redis.IRedis;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.concurrent.TimeUnit;

/**
 * 缓存操作的相关工具类
 * <p/>
 */
public final class CacheUtils {

    private CacheUtils() {

    }

    /**
     * 生成key
     *
     * @param key
     * @param values
     * @return
     */
    public static String genCacheKey(String key, Object... values) {
        return String.format(key, values);
    }

    /**
     * 获得指定名字的IRedis : 这里如果发现缓存服务不存在，暂时直接抛出异常，不允许访问未定义的缓存服务
     *
     * @param name
     * @return
     */
    public static IRedis getRedisCacheClient(INameResourceService<IRedis> cacheService, String name) {
        try {
            IRedis redis = cacheService.get(name);
            if (redis == null) {
                throw new IllegalArgumentException(String.format("unkown cache service name like %s", name));
            }
            return redis;
        } catch (Exception e) {
            throw new RuntimeException("Cache Client get exception. name :" + name, e);
        }
    }

    /**
     * 将数据{@code object } 缓存起来
     *
     * @param id             数据的ID
     * @param object         被缓存的对象
     * @param cacheParameter 缓存配置信息
     * @param cacheService   缓存服务
     * @param <T>            被缓存对象的类型.
     */
    public static <T> void set(long id, T object, CacheParameter cacheParameter, INameResourceService<IRedis> cacheService) {
        Preconditions.checkNotNull(cacheParameter);
        Preconditions.checkNotNull(cacheService);
        set(String.valueOf(id), object, cacheParameter, cacheService);
    }

    /**
     * 将数据{@code object } 缓存起来
     *
     * @param id             数据的ID
     * @param object         被缓存的对象
     * @param cacheParameter 缓存配置信息
     * @param cacheService   缓存服务
     * @param <T>            被缓存对象的类型.
     */
    public static <T> void set(String id, T object, CacheParameter cacheParameter, INameResourceService<IRedis> cacheService) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(id));
        Preconditions.checkNotNull(cacheParameter);
        Preconditions.checkNotNull(cacheService);

        IRedis redis = CacheUtils.getRedisCacheClient(cacheService, cacheParameter.getRedisServiceGroup());
        redis.setObject(cacheParameter.getCacheKey(id), object, cacheParameter.getRedisExpireSecond());
    }

    /**
     * 根据缓存对象的ID删除缓存
     *
     * @param id             要删除的缓存对象的ID
     * @param cacheParameter 缓存配置信息
     * @param cacheService   缓存服务
     */
    public static void delete(long id, CacheParameter cacheParameter, INameResourceService<IRedis> cacheService) {
        Preconditions.checkNotNull(cacheParameter);
        Preconditions.checkNotNull(cacheService);
        delete(String.valueOf(id), cacheParameter, cacheService);
    }

    /**
     * 根据缓存对象的ID删除缓存
     *
     * @param id             要删除的缓存对象的ID
     * @param cacheParameter 缓存配置信息
     * @param cacheService   缓存服务
     */
    public static void delete(String id, CacheParameter cacheParameter, INameResourceService<IRedis> cacheService) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(id));
        Preconditions.checkNotNull(cacheParameter);
        Preconditions.checkNotNull(cacheService);

        IRedis redis = CacheUtils.getRedisCacheClient(cacheService, cacheParameter.getRedisServiceGroup());
        redis.del(cacheParameter.getCacheKey(id));
    }

    /**
     * 从缓存中获取一个对象 延长缓存时间
     *
     * @param id             缓存对象的ID
     * @param cacheParameter 缓存的配置信息
     * @param cacheService   缓存服务
     * @param <T>            缓存的对象的类型
     * @return
     */
    public static <T> T get(long id, CacheParameter cacheParameter, INameResourceService<IRedis> cacheService) {
        Preconditions.checkNotNull(cacheParameter);
        Preconditions.checkNotNull(cacheService);
        return get(String.valueOf(id), cacheParameter, cacheService);
    }

    /**
     * 从缓存中获取一个对象
     *
     * @param id             缓存对象的ID
     * @param cacheParameter 缓存的配置信息
     * @param cacheService   缓存服务
     * @param isExtend       是否延长缓存时间 true为延长 false为不延长
     * @param <T>            缓存的对象的类型
     * @return
     */
    public static <T> T get(long id, CacheParameter cacheParameter, INameResourceService<IRedis> cacheService, boolean isExtend) {
        Preconditions.checkNotNull(cacheParameter);
        Preconditions.checkNotNull(cacheService);
        return get(String.valueOf(id), cacheParameter, cacheService, isExtend);
    }


    /**
     * 从缓存中获取一个对象
     *
     * @param id             缓存对象的ID
     * @param cacheParameter 缓存的配置信息
     * @param cacheService   缓存服务
     * @param <T>            缓存的对象的类型
     * @return
     */
    public static <T> T get(String id, CacheParameter cacheParameter, INameResourceService<IRedis> cacheService) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(id));
        Preconditions.checkNotNull(cacheParameter);
        Preconditions.checkNotNull(cacheService);
        return get(id, cacheParameter, cacheService, true);
    }

    /**
     * 从缓存中获取一个对象
     *
     * @param id             缓存对象的ID
     * @param cacheParameter 缓存的配置信息
     * @param cacheService   缓存服务
     * @param isExtend       是否延长缓存时间 true为延长 false为不延长
     * @param <T>            缓存的对象的类型
     * @return
     */
    public static <T> T get(String id, CacheParameter cacheParameter, INameResourceService<IRedis> cacheService, boolean isExtend) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(id));
        Preconditions.checkNotNull(cacheParameter);
        Preconditions.checkNotNull(cacheService);
        int expireSecond = 0;
        if (isExtend) {
            expireSecond = cacheParameter.getRedisExpireSecond();
        }
        T result = null;
        IRedis redis = CacheUtils.getRedisCacheClient(cacheService, cacheParameter.getRedisServiceGroup());
        result = redis.getObject(cacheParameter.getCacheKey(id), expireSecond);
        return result;
    }

    /**
     * 解析Redis部分数据结构新增或更新操作的结果
     * <p>
     * 1.Hashs
     * <p>
     * 0：表示之前有数据，仅仅工薪
     * 1：表示新建数据
     *
     * @param result
     * @return
     */
    public static boolean parseAddOrUpdateResult(long result) {
        return result == 0 || result == 1;
    }

    /**
     * 构建使用本地缓存的loading cache
     *
     * @param expireMinutes 缓存的过期时间，单位分钟
     * @param limit         缓存上限
     * @param loadFunc      根据K加载数据的函数
     * @param registry      本地缓存注册
     * @param type          本地缓存的类型
     * @param keyConverter  缓存变更的key转换函数
     * @param <K>           缓存key类型
     * @param <V>           缓存值类型
     * @return
     */
    public static <K, V> LoadingCache<K, V> createLocalLoadingCache(int expireMinutes, long limit, Function<K, V> loadFunc,
                                                                    LocalCacheRegistry registry, LocalCacheType type, Function<Object, Object> keyConverter) {
        LoadingCache<K, V> loadingCache = CacheBuilder.newBuilder().expireAfterWrite(expireMinutes, TimeUnit.MINUTES).maximumSize(limit).build(
                new CacheLoader<K, V>() {
                    @Override
                    public V load(K id) throws Exception {
                        return loadFunc.apply(id);
                    }
                });
        registry.register(type, loadingCache, keyConverter);
        return loadingCache;
    }
}

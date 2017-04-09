package com.babyfs.tk.service.biz.base;

import com.babyfs.tk.service.biz.constants.CacheConst;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.babyfs.tk.commons.base.Pair;
import com.babyfs.tk.dal.orm.IEntity;
import com.babyfs.tk.service.basic.INameResourceService;
import com.babyfs.tk.service.basic.redis.IRedis;
import com.babyfs.tk.service.biz.cache.CacheParameter;
import com.babyfs.tk.service.biz.cache.CacheUtils;

/**
 *
 */
public abstract class DataServiceUtil {
    private DataServiceUtil() {

    }


    /**
     * 根据唯一key查找id,然后根据id再加载实体
     *
     * @param key               key,非空
     * @param keyCacheParameter key和id对应的缓存配置,非空
     * @param redisService      redis缓存服务,非空
     * @param keyLoader         根据key加载实体的加载器,非空
     * @param rechecker         验证key是否与实体匹配的验证器,可以为空
     * @param getByIdLoader     根据id加载
     * @return 操作结果
     */
    public static <T extends IEntity> T getByUniqKeyAndThenById(final String key,
                                                                final CacheParameter keyCacheParameter,
                                                                final INameResourceService<IRedis> redisService,
                                                                final Function<String, T> keyLoader,
                                                                final Function<Pair<String, T>, Boolean> rechecker,
                                                                final Function<Long, T> getByIdLoader) {

        return getByUniqKeyThenIdWithNullProtect(key, keyCacheParameter, redisService, keyLoader, rechecker, getByIdLoader, false, 0);
    }

    /**
     * 根据唯一key查找id,然后根据id再加载实体,如果key不存在,则构建一个nullkey,将nullkey设置为-1,有效期为个1分钟
     *
     * @param key                     key,非空
     * @param keyCacheParameter       key和id对应的缓存配置,非空
     * @param redisService            redis缓存服务,非空
     * @param keyLoader               根据key加载实体的加载器,非空
     * @param rechecker               验证key是否与实体匹配的验证器,可以为空
     * @param getByIdLoader           根据id加载
     * @param nullProtect             是否需要null保护
     * @param nullProtectExpireSecond 空保护的有效期,单位秒
     * @return 操作结果
     * @see CacheConst#nullProtectKey(String)
     * @see #deleteNullKey(String, CacheParameter, INameResourceService)
     */
    public static <T extends IEntity> T getByUniqKeyThenIdWithNullProtect(final String key,
                                                                          final CacheParameter keyCacheParameter,
                                                                          final INameResourceService<IRedis> redisService,
                                                                          final Function<String, T> keyLoader,
                                                                          final Function<Pair<String, T>, Boolean> rechecker,
                                                                          final Function<Long, T> getByIdLoader,
                                                                          boolean nullProtect,
                                                                          int nullProtectExpireSecond) {
        Preconditions.checkNotNull(keyLoader);
        if (Strings.isNullOrEmpty(key)) {
            return null;
        }

        //根据key查找id
        Long id = CacheUtils.get(key, keyCacheParameter, redisService);
        if (id == null) {
            final String nullProtectKey = CacheConst.nullProtectKey(key);

            if (nullProtect) {
                Long nullId = CacheUtils.get(nullProtectKey, keyCacheParameter, redisService, false);
                if (nullId != null) {
                    return null;
                }
            }

            T entity = keyLoader.apply(key);
            if (entity != null) {
                CacheUtils.set(key, entity.getId(), keyCacheParameter, redisService);
                return entity;
            } else {
                if (nullProtect) {
                    // 如果未找到,则将id设置为-1L,缓存有效期由参数决定
                    CacheUtils.set(nullProtectKey, -1L, keyCacheParameter.of(nullProtectExpireSecond), redisService);
                }
            }
        } else {
            // 如果id<=0,认为是无效的缓存,直接返回
            if (id <= 0) {
                return null;
            }
            T entity = getByIdLoader.apply(id);
            if (entity != null) {
                // 重新检查key是否与实体对应
                if (rechecker != null) {
                    Boolean check = rechecker.apply(Pair.of(key, entity));
                    if (check == null || !check) {
                        CacheUtils.delete(key, keyCacheParameter, redisService);
                        T recheckEntity = keyLoader.apply(key);
                        if (recheckEntity != null) {
                            CacheUtils.set(key, recheckEntity.getId(), keyCacheParameter, redisService);
                        }
                        return entity;
                    }
                }
                return entity;
            }
        }
        return null;
    }

    /**
     * 删除Null key
     *
     * @param key
     * @param keyCacheParameter
     * @param redisService
     */
    public static void deleteNullKey(final String key,
                                     final CacheParameter keyCacheParameter,
                                     final INameResourceService<IRedis> redisService) {
        final String nullProtectKey = CacheConst.nullProtectKey(key);
        CacheUtils.delete(nullProtectKey, keyCacheParameter, redisService);
    }
}

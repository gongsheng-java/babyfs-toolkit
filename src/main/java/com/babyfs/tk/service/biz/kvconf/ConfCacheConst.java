package com.babyfs.tk.service.biz.kvconf;

import com.babyfs.tk.service.biz.cache.CacheParameter;
import com.babyfs.tk.service.biz.constants.CacheConst;
import com.babyfs.tk.service.biz.kvconf.model.KVConfEntity;
/**
 * 配置缓存配置
 */
public final class ConfCacheConst extends CacheConst{
    /**
     * {@value}
     */
    private static final String PREFIX = CacheConst.CONF_KEY_PREFIX;

    private static final int CACHE_SECONDS = CacheConst.ONE_HOUR_ENTITY_CACHE_SECONDS;
    private static final String REDIS_CACHE_GROUP = CacheConst.DEFAULT_ENTITY_CACHE_GROUP;

    private static final String CONF_KEY_PREFIX = PREFIX + "e_";
    private static final int CONF_CACHE_SECONDS = CACHE_SECONDS;
    private static final String CONF_REDIS_CACHE_GROUP = REDIS_CACHE_GROUP;

    /**
     * {@link KVConfEntity}的缓存配置
     */
    public static final CacheParameter CONF_CACHE_PARAM = new CacheParameter(CONF_CACHE_SECONDS, CONF_REDIS_CACHE_GROUP, CONF_KEY_PREFIX, "");

    private static final String CONF_NAME_KEY_PREFIX = PREFIX + "n_";
    private static final int CONF_NAME_CACHE_SECONDS = CACHE_SECONDS * 2;
    private static final String CONF_NAME_REDIS_CACHE_GROUP = REDIS_CACHE_GROUP;

    /**
     * 以{@link KVConfEntity#getName()}为key的缓存
     */
    public static final CacheParameter CONF_NAME_CACHE_PARAM = new CacheParameter(CONF_NAME_CACHE_SECONDS, CONF_NAME_REDIS_CACHE_GROUP, CONF_NAME_KEY_PREFIX, "");

    private ConfCacheConst() {
    }
}

package com.babyfs.tk.service.biz.constants;

import com.babyfs.tk.service.biz.cache.CacheParameter;

/**
 * 管理Cache的前缀
 */
public class CacheConst {
    /**
     * 默认的Entity缓存失效时间: 1小时
     */
    public static final int ONE_HOUR_ENTITY_CACHE_SECONDS = 60 * 60;
    /**
     * 缓存失效时间: 1 天
     */
    public static final int ONE_DAY_ENTITY_CACHE_SECONDS = 24 * ONE_HOUR_ENTITY_CACHE_SECONDS;
    /**
     * 默认列表缓存失效时间: 12小时
     */
    public static final int DEFAULT_LIST_CACHE_SECONDS = 12 * ONE_HOUR_ENTITY_CACHE_SECONDS;
    /**
     * Null保护key前缀
     */
    public static final String NULL_PREFIX = "_NULL";
    /**
     * KV配置相关统一前缀
     */
    public static final String CONF_KEY_PREFIX = "cf_";
    /**
     * Freq相关缓存统一前缀
     */
    public static final String FREQ_KEY_PREFIX = "fq_";
    /**
     * 短信相关缓存统一前缀
     */
    public static final String SMS_KEY_PREFIX = "sms_";
    /**
     * 临时计数器统一前缀
     */
    public static final String TEMPOAL_COUNTER_PREFIX = "tcc_";
    /**
     * Entity相关的缓存组
     */
    public static final String DEFAULT_ENTITY_CACHE_GROUP = "cache_entity";
    /**
     * 列表相关的缓存组
     */
    public static final String DEFAULT_LIST_CACHGE_GROUP = "cache_list";
    /**
     * 计数相关的缓存组
     */
    public static final String DEFAULT_COUNTER_GROUP = "counter";
    /**
     * 每日计数服务的缓存组
     */
    public static final String DEFAULT_DAILY_COUNTER_GROUP = "daily.counter";
    /**
     * 暂存的计数器,用于非关键业务计数
     */
    public static final String DEFAULT_TEMPORAL_COUNTER_GROUP = "temporal_counter";
    /**
     * Freq相关的缓存组
     */
    public static final String REDIS_CACHE_GROUP_FREQ = DEFAULT_ENTITY_CACHE_GROUP;

    protected CacheConst() {

    }

    /**
     * 使用默认group {@value #DEFAULT_ENTITY_CACHE_GROUP}和默认过期时间{@value #ONE_HOUR_ENTITY_CACHE_SECONDS}秒构建实体缓存参数
     *
     * @param mainPrefix
     * @param subPrefix
     * @return
     */
    public static CacheParameter buildEntityParameter(String mainPrefix, String subPrefix) {
        String prefix = mainPrefix + subPrefix + "_";
        return new CacheParameter(CacheConst.ONE_HOUR_ENTITY_CACHE_SECONDS, CacheConst.DEFAULT_ENTITY_CACHE_GROUP, prefix, "");
    }

    /**
     * 使用默认group {@value #DEFAULT_LIST_CACHGE_GROUP}和默认过期时间{@value #ONE_HOUR_ENTITY_CACHE_SECONDS}秒构建列表缓存参数
     *
     * @param mainPrefix
     * @param subPrefix
     * @return
     */
    public static CacheParameter buildListParameter(String mainPrefix, String subPrefix) {
        String prefix = mainPrefix + subPrefix + "_l_";
        return new CacheParameter(CacheConst.DEFAULT_LIST_CACHE_SECONDS, CacheConst.DEFAULT_LIST_CACHGE_GROUP, prefix, "");
    }

    /**
     * 使用默认group {@value #DEFAULT_COUNTER_GROUP}和默认过期时间{@value #ONE_HOUR_ENTITY_CACHE_SECONDS}构建列表计数缓存参数
     *
     * @param mainPrefix
     * @param subPrefix
     * @return
     */
    public static CacheParameter buildListCounterParameter(String mainPrefix, String subPrefix) {
        String prefix = mainPrefix + subPrefix + "_lc_";
        return new CacheParameter(CacheConst.DEFAULT_LIST_CACHE_SECONDS, CacheConst.DEFAULT_COUNTER_GROUP, prefix, "");
    }

    /**
     * 生成用户null保护的key
     *
     * @param key
     * @return
     */
    public static String nullProtectKey(String key) {
        return NULL_PREFIX + key;
    }

}

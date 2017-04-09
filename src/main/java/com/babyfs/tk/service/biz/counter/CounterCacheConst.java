package com.babyfs.tk.service.biz.counter;


import com.babyfs.tk.service.biz.cache.CacheParameter;
import com.babyfs.tk.service.biz.constants.CacheConst;

/**
 * 计数缓存配置
 */
public final class CounterCacheConst {
    /**
     * {@value}
     */
    private static final String PREFIX = CacheConst.TEMPOAL_COUNTER_PREFIX;

    private static final String DEFAULT_PERFIX = "d";
    /**
     * 默认的临时计数缓存配置,有效期一个月
     */
    public static final CacheParameter DEFAULT_TEMPROAL_COUNTER_CACHE_PARAM = buildCounterParameter(DEFAULT_PERFIX);

    private CounterCacheConst() {
    }

    public static CacheParameter buildCounterParameter(String prefix) {
        return new CacheParameter(CacheConst.ONE_HOUR_ENTITY_CACHE_SECONDS * 24 * 30, CacheConst.DEFAULT_TEMPORAL_COUNTER_GROUP, PREFIX, prefix);
    }
}

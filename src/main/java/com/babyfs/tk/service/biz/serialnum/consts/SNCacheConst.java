package com.babyfs.tk.service.biz.serialnum.consts;

import com.babyfs.tk.service.biz.cache.CacheParameter;
import com.babyfs.tk.service.biz.constants.CacheConst;

/**
 * 流水服务缓存配置
 */
public final class SNCacheConst extends CacheConst {
    /**
     * 流水服务相关前缀
     */
    private static final String PREFIX = CacheConst.SERIAL_NUM_KEY_PREFIX;

    /**
     * 流水服务递增前缀
     */
    public static final String SN_INCR_PREFIX = PREFIX + "ic_";

    public static final CacheParameter SN_INCR_CACHE_PARAM = new CacheParameter(0, DEFAULT_ENTITY_CACHE_GROUP, SN_INCR_PREFIX, "");

    private SNCacheConst() {
    }
}

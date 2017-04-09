package com.babyfs.tk.service.biz.verify;

import com.babyfs.tk.service.biz.cache.CacheParameter;
import com.babyfs.tk.service.biz.constants.CacheConst;

public final class VerifyCacheConst {
    /**
     * 定义验证SMS相关的缓存策略:失效时间5分钟
     */
    public static final CacheParameter SMS_CODE_CACHE_PARAM = new CacheParameter(5 * 60, CacheConst.DEFAULT_ENTITY_CACHE_GROUP, CacheConst.SMS_KEY_PREFIX, "");


    private VerifyCacheConst() {
    }
}

package com.babyfs.tk.service.biz.freq;

import com.babyfs.tk.commons.Constants;
import com.babyfs.tk.service.biz.cache.CacheParameter;
import com.babyfs.tk.service.biz.constants.CacheConst;
import com.google.common.hash.Hashing;

/**
 * FreqConst
 */
public final class FreqConst {

    private FreqConst() {

    }

    /**
     * 频率频次控制的公用 CacheParameter
     */
    public static final CacheParameter FREQUENCY_CACHE_PARAM = new CacheParameter(0, CacheConst.REDIS_CACHE_GROUP_FREQ, CacheConst.FREQ_KEY_PREFIX, "");

    /**
     * Redis lua 脚本：处理频次频率相关缓存
     */
    public static final String INCR_WITH_EXCPIRE_SCRIPT = "local current\n" +
            "current = redis.call(\"incr\",KEYS[1])\n" +
            "if tonumber(current) == 1 then\n" +
            "    redis.call(\"expire\",KEYS[1],ARGV[1])\n" +
            "end\n" +
            "return current";

    /**
     * 脚本和编码进行 sha1 处理
     */
    public static final String INCR_WITH_EXCPIRE_SCRIPT_SHA1 = Hashing.sha1().hashString(INCR_WITH_EXCPIRE_SCRIPT, Constants.UTF8_CHARSET).toString();
}

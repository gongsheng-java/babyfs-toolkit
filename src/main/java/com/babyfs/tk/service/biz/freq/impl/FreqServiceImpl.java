package com.babyfs.tk.service.biz.freq.impl;

import com.babyfs.tk.service.basic.INameResourceService;
import com.babyfs.tk.service.basic.guice.annotation.ServiceRedis;
import com.babyfs.tk.service.basic.redis.IRedis;
import com.babyfs.tk.service.biz.cache.CacheParameter;
import com.babyfs.tk.service.biz.cache.CacheUtils;
import com.babyfs.tk.service.biz.freq.FreqConst;
import com.babyfs.tk.service.biz.freq.FreqParameter;
import com.babyfs.tk.service.biz.freq.IFreqService;
import com.google.inject.Inject;
import org.elasticsearch.common.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 */
public class FreqServiceImpl implements IFreqService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FreqServiceImpl.class);

    @Inject
    @ServiceRedis
    INameResourceService<IRedis> redisService;

    private static final String REDIS_KEY_TEMPLATE = "%d_%s";
    private static CacheParameter cacheParameter = FreqConst.FREQUENCY_CACHE_PARAM;

    @Override
    public boolean checkAndUpdate(String key, FreqParameter freqParameter) {
        int expireSecond = freqParameter.getExpireSecond();
        boolean isUpdateExpire = freqParameter.isUpdateExpire();
        String cacheKey = buildCacheKey(freqParameter.getType(), key);
        IRedis redis = CacheUtils.getRedisCacheClient(redisService, cacheParameter.getRedisServiceGroup());
        String result = redis.get(cacheKey);

        if (result != null) {
            if (isExceedTimeLimit(key, freqParameter, result)) {
                return false;
            }
        }

        if (isUpdateExpire) {
            redis.incr(cacheKey, expireSecond);
        } else {
            redis.eval(cacheKey, FreqConst.INCR_WITH_EXCPIRE_SCRIPT, FreqConst.INCR_WITH_EXCPIRE_SCRIPT_SHA1, String.valueOf(expireSecond));
        }
        return true;
    }

    @Override
    public boolean check(String key, FreqParameter freqParameter) {
        String cacheKey = buildCacheKey(freqParameter.getType(), key);
        IRedis redis = CacheUtils.getRedisCacheClient(redisService, cacheParameter.getRedisServiceGroup());
        String result = redis.get(cacheKey);

        if (!Strings.isNullOrEmpty(result)) {
            if (isExceedTimeLimit(key, freqParameter, result)) {
                return false;
            }
        }
        return true;
    }


    @Override
    public void clean(String key, FreqParameter freqParameter) {
        CacheUtils.delete(buildCacheKey(freqParameter.getType(), key), cacheParameter, redisService);
    }

    /**
     * 构建Redis Key
     *
     * @param type
     * @param key
     * @return
     */
    private String buildCacheKey(int type, String key) {
        return cacheParameter.getCacheKey(String.format(REDIS_KEY_TEMPLATE, type, key));
    }

    /**
     * 是否超过频次限制
     *
     * @param key
     * @param freqParameter
     * @param result
     * @return
     */
    private boolean isExceedTimeLimit(String key, FreqParameter freqParameter, String result) {
        try {
            long resultTime = Long.parseLong(result);
            if (resultTime >= freqParameter.getTimeLimit()) {
                return true;
            }
        } catch (NumberFormatException e) {
            LOGGER.error("Invalid Key." + key + " " + result);
            return true;
        }
        return false;
    }
}

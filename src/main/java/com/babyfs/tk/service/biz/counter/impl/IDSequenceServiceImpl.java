package com.babyfs.tk.service.biz.counter.impl;

import com.babyfs.tk.service.biz.constants.CacheConst;
import com.babyfs.tk.service.biz.counter.CounterInitializer;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.babyfs.tk.service.basic.INameResourceService;
import com.babyfs.tk.service.basic.guice.annotation.ServiceRedis;
import com.babyfs.tk.service.basic.redis.IRedis;
import com.babyfs.tk.service.biz.counter.IDSequenceService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * 计数服务实现
 * <p/>
 */
public class IDSequenceServiceImpl implements IDSequenceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(IDSequenceServiceImpl.class);

    private static final FastDateFormat YYMMDD_FORMAT = FastDateFormat.getInstance("yyyyMMdd");

    @Inject
    @ServiceRedis
    private INameResourceService<IRedis> redisService;

    @Override
    public long getNext(String key) throws Exception {
        if (Strings.isNullOrEmpty(key)) {
            LOGGER.error("getNext : parameter key to get next id is null or empty");
            return INVALID_ID;
        }
        try {
            IRedis redis = redisService.get(CacheConst.DEFAULT_COUNTER_GROUP);
            return redis.incr(key);
        } catch (Exception e) {
            LOGGER.error("Exception occured in Redis", e);
            throw new RuntimeException("Exception occured in Redis", e);
        }
    }

    @Override
    public long getNext(String key, CounterInitializer counterInitializer) throws Exception{
        if (Strings.isNullOrEmpty(key)) {
            LOGGER.error("getNext : parameter key to get next id is null or empty");
            return INVALID_ID;
        }
        try {
            IRedis redis = redisService.get(CacheConst.DEFAULT_COUNTER_GROUP);
            long nextId = redis.incr(key);
            if(nextId == 1 && counterInitializer != null){
                try{
                    nextId = counterInitializer.getStart();
                    redis.set(key, String.valueOf(nextId), 0);
                }catch (Exception ex){
                    LOGGER.error("init counter error key " + key, ex);
                    //初始化失败的时候，设置为0，可触发多次初始化
                    redis.set(key, "0", 0);
                }
            }
            return nextId;
        } catch (Exception e) {
            LOGGER.error("Exception occured in Redis", e);
            throw new RuntimeException("Exception occured in Redis", e);
        }
    }

    @Override
    public long getNext(String key,int expireSeconds) throws Exception {
        if (Strings.isNullOrEmpty(key)) {
            LOGGER.error("getNext : parameter key to get next id is null or empty");
            return INVALID_ID;
        }
        try {
            IRedis redis = redisService.get(CacheConst.DEFAULT_COUNTER_GROUP);
            return redis.incr(key,expireSeconds);
        } catch (Exception e) {
            LOGGER.error("Exception occured in Redis", e);
            throw new RuntimeException("Exception occured in Redis", e);
        }
    }

    @Override
    public void resetCounter(String key) throws Exception {
        if (Strings.isNullOrEmpty(key)) {
            LOGGER.error("resetCounter : parameter key to get next id is null or empty");
        }
        try {
            IRedis redis = redisService.get(CacheConst.DEFAULT_COUNTER_GROUP);
            redis.set(key, "0", 0);
        } catch (Exception e) {
            LOGGER.error("Exception occured in Redis", e);
            throw new RuntimeException("Exception occured in Redis", e);
        }
    }

    @Override
    public long getDailyNext(String key, Date date) throws Exception {
        key = Preconditions.checkNotNull(StringUtils.trimToNull(key), "The key must not be null or empty");
        date = Preconditions.checkNotNull(date, "The date must not be null");

        String redisKey = YYMMDD_FORMAT.format(date) + "_" + key;
        try {
            IRedis redis = redisService.get(CacheConst.DEFAULT_DAILY_COUNTER_GROUP);
            return redis.incr(redisKey, CacheConst.ONE_DAY_ENTITY_CACHE_SECONDS*2);
        } catch (Exception e) {
            LOGGER.error("Exception occured in Redis", e);
            throw new RuntimeException("Exception occured in Redis", e);
        }
    }

}

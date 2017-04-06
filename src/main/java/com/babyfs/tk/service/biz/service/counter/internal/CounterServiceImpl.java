package com.babyfs.tk.service.biz.service.counter.internal;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.babyfs.tk.service.basic.INameResourceService;
import com.babyfs.tk.service.basic.guice.annotation.ServiceRedis;
import com.babyfs.tk.service.basic.redis.IRedis;
import com.babyfs.tk.service.biz.service.counter.ICounterService;
import com.babyfs.tk.service.biz.service.counter.constants.CounterConst;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * 计数服务实现
 * <p/>
 */
public class CounterServiceImpl implements ICounterService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CounterServiceImpl.class);

    private static final FastDateFormat YYMMDD_FORMAT = FastDateFormat.getInstance("yyyyMMdd");

    @Inject
    @ServiceRedis
    private INameResourceService<IRedis> redisService;

    @Override
    public long getNext(String key) throws Exception {
        if (Strings.isNullOrEmpty(key)) {
            LOGGER.error("getNext : parameter key to get next id is null or empty");
            return CounterConst.INVALID_ID;
        }
        try {
            IRedis redis = redisService.get(CounterConst.CACHE_GROUP_COUNTER_SERVICE);
            return redis.incr(key);
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
            IRedis redis = redisService.get(CounterConst.CACHE_GROUP_COUNTER_SERVICE);
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
            IRedis redis = redisService.get(CounterConst.CACHE_GROUP_DAILY_COUNTER);
            return redis.incr(redisKey, CounterConst.CACHE_TIME_DAILY_COUNTER);
        } catch (Exception e) {
            LOGGER.error("Exception occured in Redis", e);
            throw new RuntimeException("Exception occured in Redis", e);
        }
    }

}

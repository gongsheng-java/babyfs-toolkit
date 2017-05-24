package com.babyfs.tk.service.biz.counter.guice;

import com.babyfs.tk.service.biz.counter.IWithRedisCounterService;
import com.babyfs.tk.service.biz.counter.impl.RedisCounterService;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Set;

/**
 * 需要同步的计数器同步
 */
public abstract class SyncServcieRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(SyncServcieRegistry.class);

    public Set<RedisCounterService> getRedisCounterServices() {
        Set<RedisCounterService> counterServices = Sets.newHashSet();
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object o = field.get(this);
                if (RedisCounterService.class.isInstance(o)) {
                    counterServices.add((RedisCounterService) o);
                } else if (IWithRedisCounterService.class.isInstance(o)) {
                    IWithRedisCounterService redisCounter = (IWithRedisCounterService) o;
                    counterServices.addAll(redisCounter.getRedisCounterService());
                }
            } catch (Exception e) {
                LOGGER.error("match sync service fail", e);
            }
        }
        return counterServices;
    }

}

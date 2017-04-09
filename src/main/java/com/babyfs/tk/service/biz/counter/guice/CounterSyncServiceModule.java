package com.babyfs.tk.service.biz.counter.guice;

import com.babyfs.tk.commons.service.ServiceModule;
import com.babyfs.tk.service.biz.counter.IWithRedisCounterService;
import com.babyfs.tk.service.biz.counter.impl.RedisCounterService;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Set;


/**
 *
 */
public class CounterSyncServiceModule extends ServiceModule {
    @Override
    protected void configure() {
        bind(SyncServcieRegistry.class).asEagerSingleton();
    }

    public static class SyncServcieRegistry {
        private static final Logger LOGGER = LoggerFactory.getLogger(SyncServcieRegistry.class);

        /**
         * CounterSyncServiceModule
         * 取得所有的redis counter service
         *
         * @return
         */
        public Set<RedisCounterService> getRedisCounterServices() {
            Set<RedisCounterService> counterServices = Sets.newHashSet();
            Field[] fields = this.getClass().getDeclaredFields();
            for (Field field : fields) {
                try {
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
}

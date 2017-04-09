package com.babyfs.tk.service.biz.counter.guice;

import com.babyfs.tk.commons.service.ServiceModule;
import com.babyfs.tk.service.basic.INameResourceService;
import com.babyfs.tk.service.basic.guice.annotation.ServiceRedis;
import com.babyfs.tk.service.basic.redis.IRedis;
import com.babyfs.tk.service.biz.counter.CounterCacheConst;
import com.babyfs.tk.service.biz.counter.ITemporalCounterService;
import com.babyfs.tk.service.biz.counter.impl.RedisTemporalCounterService;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * {@link ITemporalCounterService}
 */
public class TemporalCounterServiceModule extends ServiceModule {
    @Override
    protected void configure() {
        bindServiceWithProvider(ITemporalCounterService.class, DefaultTremporalCounterServiceProvider.class);
    }

    public static class DefaultTremporalCounterServiceProvider implements Provider<ITemporalCounterService> {
        @Inject
        @ServiceRedis
        INameResourceService<IRedis> cacheServcie;

        @Override
        public ITemporalCounterService get() {
            return new RedisTemporalCounterService(CounterCacheConst.DEFAULT_TEMPROAL_COUNTER_CACHE_PARAM, cacheServcie);
        }
    }
}

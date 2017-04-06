package com.babyfs.tk.service.biz.service.counter.intergration.guice;

import com.babyfs.tk.commons.service.ServiceModule;
import com.babyfs.tk.service.biz.service.counter.ICounterService;
import com.babyfs.tk.service.biz.service.counter.internal.CounterServiceImpl;

/**
 * 计数服务Module
 * <p/>
 */
public class CounterServiceModule extends ServiceModule {

    @Override
    protected void configure() {
        bindService(ICounterService.class, CounterServiceImpl.class);
    }

}

package com.babyfs.tk.service.biz.service.counter.intergration.guice;

import com.babyfs.tk.rpc.guice.ClientServiceModule;
import com.babyfs.tk.service.biz.service.counter.ICounterService;

/**
 * 计数服务RpcClientModule
 * <p/>
 */
public class CounterServiceClientModule extends ClientServiceModule {

    @Override
    protected void configure() {
        bindService(ICounterService.class);
    }

}

package com.babyfs.tk.service.biz.counter.guice;

import com.babyfs.tk.rpc.guice.ClientServiceModule;
import com.babyfs.tk.service.biz.counter.IDSequenceService;

/**
 * 计数服务RpcClientModule
 * <p/>
 */
public class CounterServiceClientModule extends ClientServiceModule {

    @Override
    protected void configure() {
        bindService(IDSequenceService.class);
    }

}

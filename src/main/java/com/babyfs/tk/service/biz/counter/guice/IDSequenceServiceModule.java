package com.babyfs.tk.service.biz.counter.guice;

import com.babyfs.tk.commons.service.ServiceModule;
import com.babyfs.tk.service.biz.counter.impl.IDSequenceServiceImpl;
import com.babyfs.tk.service.biz.counter.IDSequenceService;

/**
 * 计数服务Module
 * <p/>
 */
public class IDSequenceServiceModule extends ServiceModule {

    @Override
    protected void configure() {
        bindService(IDSequenceService.class, IDSequenceServiceImpl.class);
    }

}

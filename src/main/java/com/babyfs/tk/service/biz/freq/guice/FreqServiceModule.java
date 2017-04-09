package com.babyfs.tk.service.biz.freq.guice;


import com.babyfs.tk.commons.service.ServiceModule;
import com.babyfs.tk.service.biz.freq.IFreqService;
import com.babyfs.tk.service.biz.freq.impl.FreqServiceImpl;

/**
 * 频率控制
 */
public class FreqServiceModule extends ServiceModule {
    @Override
    protected void configure() {
        bindService(IFreqService.class, FreqServiceImpl.class);
    }
}

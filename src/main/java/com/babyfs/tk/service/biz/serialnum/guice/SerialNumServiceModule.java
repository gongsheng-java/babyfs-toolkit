package com.babyfs.tk.service.biz.serialnum.guice;

import com.babyfs.tk.commons.service.ServiceModule;
import com.babyfs.tk.service.biz.serialnum.ISerialNumService;
import com.babyfs.tk.service.biz.serialnum.impl.SerialNumServiceImpl;

public class SerialNumServiceModule extends ServiceModule {
    @Override
    protected void configure() {
        bindService(ISerialNumService.class, SerialNumServiceImpl.class);
        install(new ZkSNServiceRegisterModule());
    }
}

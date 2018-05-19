package com.babyfs.tk.galaxy.guice.register;

import com.babyfs.tk.commons.service.ServiceModule;
import com.babyfs.tk.galaxy.register.IServcieRegister;
import com.babyfs.tk.galaxy.register.impl.LocalServiceRegister;

/**
 * {@link IServcieRegister}的本地实现
 */
public class LocalServiceRegisterModule extends ServiceModule {
    @Override
    protected void configure() {
        bindService(IServcieRegister.class, LocalServiceRegister.class);
    }
}

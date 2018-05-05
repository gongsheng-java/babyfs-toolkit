package com.babyfs.tk.galaxy.guice;

import com.babyfs.tk.commons.service.ServiceModule;
import com.babyfs.tk.galaxy.codec.IDecoder;
import com.babyfs.tk.galaxy.codec.IEncoder;
import com.babyfs.tk.galaxy.codec.impl.HessianDecoder;
import com.babyfs.tk.galaxy.codec.impl.HessianEncoder;

/**
 * 提供RPC公共的module
 */
public class RpcSupportModule extends ServiceModule {
    @Override
    protected void configure() {
        bind(IEncoder.class).to(HessianEncoder.class).asEagerSingleton();
        bind(IDecoder.class).to(HessianDecoder.class).asEagerSingleton();
    }
}

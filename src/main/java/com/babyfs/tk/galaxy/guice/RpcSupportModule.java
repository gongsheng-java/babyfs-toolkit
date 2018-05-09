package com.babyfs.tk.galaxy.guice;

import com.babyfs.tk.commons.codec.ICodec;
import com.babyfs.tk.commons.codec.impl.HessianCodec;
import com.babyfs.tk.commons.service.ServiceModule;
import com.babyfs.tk.galaxy.RpcCodec;

/**
 * 提供RPC公共的module
 */
public class RpcSupportModule extends ServiceModule {
    @Override
    protected void configure() {
        //注册code
        bind(ICodec.class).annotatedWith(RpcCodec.class).to(HessianCodec.class).asEagerSingleton();
    }
}

package com.babyfs.tk.galaxy.guice;

import com.babyfs.tk.galaxy.client.IInvocationHandlerFactory;
import com.babyfs.tk.galaxy.codec.IDecoder;
import com.babyfs.tk.galaxy.codec.IEncoder;
import com.babyfs.tk.galaxy.register.ILoadBalance;
import com.babyfs.tk.galaxy.register.IRule;
import com.babyfs.tk.galaxy.register.LoadBalanceImpl;
import com.babyfs.tk.galaxy.register.RoundRobinRule;
import com.google.inject.AbstractModule;

public class RpcSupportModel extends AbstractModule {

    @Override
    protected void configure() {
        //编码解码注入
        bind(IEncoder.class).to(IEncoder.Default.class);
        bind(IDecoder.class).to(IDecoder.Default.class);

        //负载均衡注入
        bind(IRule.class).to(RoundRobinRule.class);
        bind(ILoadBalance.class).to(LoadBalanceImpl.class);

        //反射处理工厂注入
        bind(IInvocationHandlerFactory.class).to(IInvocationHandlerFactory.Default.class);

    }
}

package com.babyfs.tk.galaxy.guice;

import com.babyfs.tk.commons.service.ServiceModule;
import com.babyfs.tk.galaxy.client.ClientProxyBuilder;
import com.babyfs.tk.galaxy.client.IClient;
import com.babyfs.tk.galaxy.client.IInvocationHandlerFactory;
import com.babyfs.tk.galaxy.client.RpcOkHttpClient;
import com.babyfs.tk.galaxy.codec.IDecoder;
import com.babyfs.tk.galaxy.codec.IEncoder;
import com.babyfs.tk.galaxy.register.ILoadBalance;
import com.babyfs.tk.galaxy.register.IRule;
import com.babyfs.tk.galaxy.register.LoadBalanceImpl;
import com.babyfs.tk.galaxy.register.RoundRobinRule;
import com.babyfs.tk.galaxy.server.IRpcService;
import com.babyfs.tk.galaxy.server.impl.RpcServiceImpl;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class RpcSupportModel extends ServiceModule {

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

        //rpc client 注入
        install(new RpcOkHttpClientModel());

        //rpc service注入
        bindService(IRpcService.class, RpcServiceImpl.class);

        //rpc 代理相关注入
        bind(ClientProxyBuilder.class).toProvider(ClientProxyBuilderProvider.class).asEagerSingleton();
        install(new MethodCacheServiceModel());
        bind(IClient.class).toProvider(IClientProvider.class);

        //zk 注册发现服务注入
        install(new ZkDiscoveryClientModule());

    }

    public static class IClientProvider implements Provider<IClient> {
        @Inject
        private RpcOkHttpClient client;

        @Override
        public IClient get() {
            return client;
        }
    }
}

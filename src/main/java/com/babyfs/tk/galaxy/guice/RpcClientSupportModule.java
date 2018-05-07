package com.babyfs.tk.galaxy.guice;

import com.babyfs.tk.commons.MapConfig;
import com.babyfs.tk.commons.codec.ICodec;
import com.babyfs.tk.commons.config.IConfigService;
import com.babyfs.tk.commons.service.ServiceModule;
import com.babyfs.tk.galaxy.RpcCodec;
import com.babyfs.tk.galaxy.client.IClient;
import com.babyfs.tk.galaxy.client.IClientProxyFactory;
import com.babyfs.tk.galaxy.client.impl.ClientProxyFactoryImpl;
import com.babyfs.tk.galaxy.client.impl.RpcOkHttpClient;
import com.babyfs.tk.galaxy.constant.RpcConstant;
import com.babyfs.tk.galaxy.register.ILoadBalance;
import com.babyfs.tk.galaxy.register.IRule;
import com.babyfs.tk.galaxy.register.impl.LoadBalanceImpl;
import com.babyfs.tk.galaxy.register.impl.RoundRobinRule;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * RPC客户端依赖的基础模块
 */
public class RpcClientSupportModule extends ServiceModule {
    private static final long DEFAULT_CONNECT_TIMEOUT = 15;

    private static final long DEFAULT_READ_TIMEOUT = 15;

    private static final long DEFAULT_WRITE_TIMEOUT = 15;

    @Override
    protected void configure() {
        bind(IRule.class).to(RoundRobinRule.class).asEagerSingleton();
        bind(ILoadBalance.class).to(LoadBalanceImpl.class).asEagerSingleton();
        bind(IClient.class).toProvider(OkHttpClientProvider.class).asEagerSingleton();
        bind(IClientProxyFactory.class).toProvider(ClientProxyFactoryProvider.class).asEagerSingleton();
        install(new ZkServiceNamesModule());
    }

    public static class OkHttpClientProvider implements Provider<IClient> {
        @Inject(optional = true)
        private IConfigService conf;

        @Override
        public IClient get() {
            RpcOkHttpClient rpcOkHttpClient = new RpcOkHttpClient();
            rpcOkHttpClient.init(MapConfig.getLong(RpcConstant.OK_CONNECT_TIMEOUT, conf, DEFAULT_CONNECT_TIMEOUT),
                    MapConfig.getLong(RpcConstant.OK_READ_TIMEOUT, conf, DEFAULT_READ_TIMEOUT),
                    MapConfig.getLong(RpcConstant.OK_WRITE_TIMEOUT, conf, DEFAULT_WRITE_TIMEOUT));
            return rpcOkHttpClient;
        }
    }

    public static class ClientProxyFactoryProvider implements Provider<IClientProxyFactory> {
        @Inject(optional = true)
        private IConfigService conf;
        /**
         * 编解码器
         */
        @Inject
        @RpcCodec
        private ICodec codec;
        /**
         * 传输层采用的Client
         */
        @Inject
        private IClient client;
        /**
         * 负载均衡器
         */
        @Inject
        private ILoadBalance loadBalance;

        @Override
        public IClientProxyFactory get() {
            String url = MapConfig.getString(RpcConstant.NAME_RPC_CLIENT_URL_PREFIX, conf, RpcConstant.RPC_URL_PREFIX_DEFAULT);
            ClientProxyFactoryImpl factory = new ClientProxyFactoryImpl(codec, client, loadBalance, url);
            return factory;
        }
    }
}

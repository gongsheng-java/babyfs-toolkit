package com.babyfs.tk.galaxy.guice;


import com.babyfs.tk.commons.MapConfig;
import com.babyfs.tk.commons.config.IConfigService;
import com.babyfs.tk.galaxy.client.ClientProxyBuilder;
import com.babyfs.tk.galaxy.client.RpcOkHttpClient;
import com.babyfs.tk.galaxy.config.RpcZkConfig;
import com.babyfs.tk.galaxy.register.IRpcConfigService;
import com.babyfs.tk.galaxy.register.LoadBalanceImpl;
import com.google.inject.Inject;
import com.google.inject.PrivateModule;
import com.google.inject.Provider;

public class ClientProxyBuilderModel extends PrivateModule {

    private static final int DEFAULT_CONNECT_TIMEOUT = 20000;
    private static final int DEFAULT_SESSION_TIMEOUT = 20000;
    private static final String DEFAULT_REGISTER_URL = "127.0.0.1:2181";
    private static final String DEFAULT_URL_PREFIX = "/rpc/invoke";

    @Override
    protected void configure() {

        bind(ClientProxyBuilder.class).toProvider(ClientProxyBuilderModel.ClientProxyBuilderProvider.class).asEagerSingleton();
        expose(ClientProxyBuilder.class);
    }

    private static class ClientProxyBuilderProvider implements Provider<ClientProxyBuilder> {

        @Inject
        private RpcOkHttpClient rpcOkHttpClient;

        @Inject
        private IRpcConfigService rpcConfig;


        @Inject(optional = true)
        private IConfigService conf;

        @Override
        public ClientProxyBuilder get() {
            LoadBalanceImpl loadBalance = LoadBalanceImpl.builder().rpcConfig(rpcConfig).build(
                    MapConfig.getString(RpcZkConfig.REGISTER_URL, conf, DEFAULT_REGISTER_URL),
                    MapConfig.getInt(RpcZkConfig.CONNECT_TIMEOUT, conf, DEFAULT_CONNECT_TIMEOUT),
                    MapConfig.getInt(RpcZkConfig.SESSION_TIMEOUT, conf, DEFAULT_SESSION_TIMEOUT));

            return ClientProxyBuilder.builder().loadBalance(loadBalance).client(rpcOkHttpClient).urlPrefix(MapConfig.getString(RpcZkConfig.URL_PREFIX, conf, DEFAULT_REGISTER_URL));
        }
    }
}

package com.babyfs.tk.galaxy.guice;

import com.babyfs.tk.commons.MapConfig;
import com.babyfs.tk.commons.config.IConfigService;
import com.babyfs.tk.galaxy.client.RpcOkHttpClient;
import com.babyfs.tk.galaxy.config.OkHttpClientConfig;
import com.google.inject.Inject;
import com.google.inject.PrivateModule;
import com.google.inject.Provider;

public class RpcOkHttpClientModel extends PrivateModule {

    private static final long DEFAULT_CONNECT_TIMEOUT = 15;

    private static final long DEFAULT_READ_TIMEOUT = 15;

    private static final long DEFAULT_WRITE_TIMEOUT = 15;

    @Override
    protected void configure() {
        bind(RpcOkHttpClient.class).toProvider(RpcOkHttpClientModel.OkHttpClientProvider.class).asEagerSingleton();
        expose(RpcOkHttpClient.class);
    }

    private static class OkHttpClientProvider implements Provider<RpcOkHttpClient> {

        @Inject(optional = true)
        private IConfigService conf;

        @Override
        public RpcOkHttpClient get() {
            RpcOkHttpClient rpcOkHttpClient = new RpcOkHttpClient();
            rpcOkHttpClient.init(MapConfig.getLong(OkHttpClientConfig.CONNECT_TIMEOUT, conf, DEFAULT_CONNECT_TIMEOUT),
                    MapConfig.getLong(OkHttpClientConfig.READ_TIMEOUT, conf, DEFAULT_READ_TIMEOUT),
                    MapConfig.getLong(OkHttpClientConfig.WRITE_TIMEOUT, conf, DEFAULT_WRITE_TIMEOUT));
            return rpcOkHttpClient;
        }
    }
}

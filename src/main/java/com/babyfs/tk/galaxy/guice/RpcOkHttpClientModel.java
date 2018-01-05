package com.babyfs.tk.galaxy.guice;

import com.babyfs.tk.commons.config.IConfigService;
import com.babyfs.tk.galaxy.client.RpcOkHttpClient;
import com.google.inject.Inject;
import com.google.inject.PrivateModule;
import com.google.inject.Provider;

public class RpcOkHttpClientModel extends PrivateModule {

    private static final long CONNECT_TIMEOUT = 5;

    private static final long READ_TIMEOUT = 5;

    private static final long WRITE_TIMEOUT = 5;

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
            rpcOkHttpClient.init(CONNECT_TIMEOUT,READ_TIMEOUT,WRITE_TIMEOUT);
            return rpcOkHttpClient;
        }
    }
}

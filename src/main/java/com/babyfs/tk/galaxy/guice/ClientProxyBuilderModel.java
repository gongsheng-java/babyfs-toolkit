package com.babyfs.tk.galaxy.guice;


import com.babyfs.tk.galaxy.client.ClientProxyBuilder;
import com.babyfs.tk.galaxy.client.RpcOkHttpClient;
import com.babyfs.tk.galaxy.register.IDiscoveryProperties;
import com.babyfs.tk.galaxy.register.LoadBalanceImpl;
import com.google.inject.Inject;
import com.google.inject.PrivateModule;
import com.google.inject.Provider;

public class ClientProxyBuilderModel extends PrivateModule {
    @Override
    protected void configure() {

        bind(ClientProxyBuilder.class).toProvider(ClientProxyBuilderModel.ClientProxyBuilderProvider.class).asEagerSingleton();
        expose(ClientProxyBuilder.class);
    }

    private static class ClientProxyBuilderProvider implements Provider<ClientProxyBuilder> {

        @Inject
        private RpcOkHttpClient rpcOkHttpClient;

        @Inject
        private IDiscoveryProperties discoveryProperties;

        @Override
        public ClientProxyBuilder get() {
            LoadBalanceImpl loadBalance = LoadBalanceImpl.builder().discoveryProperties(discoveryProperties).build();
            return ClientProxyBuilder.builder().loadBalance(loadBalance).client(rpcOkHttpClient);
        }
    }
}

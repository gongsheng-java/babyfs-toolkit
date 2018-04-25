package com.babyfs.tk.galaxy.guice;

import com.babyfs.tk.galaxy.client.ClientProxyBuilder;
import com.babyfs.tk.galaxy.client.IClient;
import com.babyfs.tk.galaxy.client.IInvocationHandlerFactory;
import com.babyfs.tk.galaxy.client.RpcOkHttpClient;
import com.babyfs.tk.galaxy.codec.IDecoder;
import com.babyfs.tk.galaxy.codec.IEncoder;
import com.babyfs.tk.galaxy.register.LoadBalanceImpl;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class ClientProxyBuilderProvider implements Provider<ClientProxyBuilder> {
    private String urlPrefix;
    //编码器
    @Inject
    private IEncoder encoder;
    //解码器
    @Inject
    private IDecoder decoder;
    //传输层采用的Client
    @Inject
    private RpcOkHttpClient client;
    //负载均衡器
    @Inject
    private LoadBalanceImpl loadBalance;
    //InvocationHandler工厂类
    @Inject
    private IInvocationHandlerFactory invocationHandlerFactory;

    public ClientProxyBuilderProvider(String urlPrefix) {
        this.urlPrefix = urlPrefix;
    }

    @Override
    public ClientProxyBuilder get() {
        return ClientProxyBuilder.builder()
                .urlPrefix(urlPrefix)
                .client(client)
                .decoder(decoder)
                .encoder(encoder)
                .loadBalance(loadBalance)
                .invocationHandlerFactory(invocationHandlerFactory);
    }
}

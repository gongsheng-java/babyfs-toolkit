package com.babyfs.tk.galaxy.guice;

import com.babyfs.tk.galaxy.client.ClientProxyBuilder;
import com.babyfs.tk.galaxy.client.IClient;
import com.babyfs.tk.galaxy.client.IInvocationHandlerFactory;
import com.babyfs.tk.galaxy.codec.IDecoder;
import com.babyfs.tk.galaxy.codec.IEncoder;
import com.babyfs.tk.galaxy.constant.RpcConstant;
import com.babyfs.tk.galaxy.register.ILoadBalance;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class ClientProxyBuilderProvider implements Provider<ClientProxyBuilder> {
    //编码器
    @Inject
    private IEncoder encoder;
    //解码器
    @Inject
    private IDecoder decoder;
    //传输层采用的Client
    @Inject
    private IClient client;
    //负载均衡器
    @Inject
    private ILoadBalance loadBalance;
    //InvocationHandler工厂类
    @Inject
    private IInvocationHandlerFactory invocationHandlerFactory;

    @Override
    public ClientProxyBuilder get() {
        return ClientProxyBuilder.builder()
                .urlPrefix(RpcConstant.RPC_URL_PREFIX_DEFAULT)
                .client(client)
                .decoder(decoder)
                .encoder(encoder)
                .loadBalance(loadBalance)
                .invocationHandlerFactory(invocationHandlerFactory);
    }
}

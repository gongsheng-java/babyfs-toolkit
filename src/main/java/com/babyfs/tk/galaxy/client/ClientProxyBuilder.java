package com.babyfs.tk.galaxy.client;

import com.babyfs.tk.galaxy.codec.Decoder;
import com.babyfs.tk.galaxy.codec.Encoder;
import com.babyfs.tk.galaxy.register.LoadBalanceImpl;

/**
 * GalaxyClientProxyBuilder
 * builder模式创建GalaxyClientProxy
 */
public class ClientProxyBuilder {

    //编码器
    private Encoder encoder = new Encoder.Default();
    //解码器
    private Decoder decoder = new Decoder.Default();
    //传输层采用的Client
    private IClient client =  null;
    //负载均衡器
    private LoadBalanceImpl loadBalance = null;
    //InvocationHandler工厂类
    private IInvocationHandlerFactory invocationHandlerFactory =
            new IInvocationHandlerFactory.Default();


    public ClientProxyBuilder encoder(Encoder encoder) {
        this.encoder = encoder;
        return this;
    }

    public ClientProxyBuilder decoder(Decoder decoder) {
        this.decoder = decoder;
        return this;
    }

    public ClientProxyBuilder client(IClient client) {
        this.client = client;
        return this;
    }

    public ClientProxyBuilder loadBalance(LoadBalanceImpl loadBalance) {
        this.loadBalance = loadBalance;
        return this;
    }

    public ClientProxyBuilder invocationHandlerFactory(IInvocationHandlerFactory invocationHandlerFactory) {
        this.invocationHandlerFactory = invocationHandlerFactory;
        return this;
    }

    /**
     * appName信息的Target对象
     * @param apiType Class对
     * @param appName 应用名称
     * @param <T>
     * @return 代理对象(HardCodedTarget)
     */
    public <T> T target(Class<T> apiType, String appName) {
        return target(new ITarget.HardCodedTarget<T>(apiType, appName));
    }

    private <T> T target(ITarget<T> target) {
        return build().newInstance(target);
    }

    public AbstractClientProxy build() {
        MethodHandler.Factory synchronousMethodHandlerFactory =
                new MethodHandler.Factory();
        ReflectiveClientProxy.ParseHandlersByName handlersByName =
                new ReflectiveClientProxy.ParseHandlersByName(encoder, decoder, client,
                        synchronousMethodHandlerFactory, loadBalance);
        return new ReflectiveClientProxy(handlersByName, invocationHandlerFactory);
    }
}

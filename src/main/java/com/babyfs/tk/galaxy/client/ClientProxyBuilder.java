package com.babyfs.tk.galaxy.client;

import com.babyfs.tk.galaxy.codec.IDecoder;
import com.babyfs.tk.galaxy.codec.IEncoder;
import com.babyfs.tk.galaxy.register.LoadBalanceImpl;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * ClientProxyBuilder
 * builder模式创建ReflectiveClientProxy
 */
public class ClientProxyBuilder {

    /**
     * 创建ClientProxyBuilder方法
     */
    public static ClientProxyBuilder builder() {
        return new ClientProxyBuilder();
    }

    //编码器
    private IEncoder encoder = new IEncoder.Default();
    //解码器
    private IDecoder decoder = new IDecoder.Default();
    //传输层采用的Client
    private IClient client = null;
    //负载均衡器
    private LoadBalanceImpl loadBalance = null;
    //InvocationHandler工厂类
    private IInvocationHandlerFactory invocationHandlerFactory =
            new IInvocationHandlerFactory.Default();

    private String urlPrefix;


    public ClientProxyBuilder encoder(IEncoder encoder) {
        this.encoder = encoder;
        return this;
    }

    public ClientProxyBuilder decoder(IDecoder decoder) {
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

    public ClientProxyBuilder urlPrefix(String urlPrefix) {
        this.urlPrefix = urlPrefix;
        return this;
    }


    /**
     * appName信息的Target对象
     *
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

    public IClientProxy build() {

        checkNotNull(client, "client");
        checkNotNull(loadBalance, "loadBalance");
        checkNotNull(urlPrefix, "urlPrefix");
        MethodHandler.Factory methodHandlerFactory = new MethodHandler.Factory();
        ReflectiveClientProxy.ParseHandlersByName parseHandlersByName =
                new ReflectiveClientProxy.ParseHandlersByName(encoder, decoder, client,
                        methodHandlerFactory, loadBalance, urlPrefix);
        return new ReflectiveClientProxy(parseHandlersByName, invocationHandlerFactory);
    }
}

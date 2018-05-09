
package com.babyfs.tk.galaxy.client.impl;


import com.babyfs.tk.commons.codec.ICodec;
import com.babyfs.tk.galaxy.ServicePoint;
import com.babyfs.tk.galaxy.Utils;
import com.babyfs.tk.galaxy.client.IClient;
import com.babyfs.tk.galaxy.client.IClientProxyFactory;
import com.babyfs.tk.galaxy.constant.RpcConstant;
import com.babyfs.tk.galaxy.register.ILoadBalance;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.elasticsearch.common.Strings;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 */
public class ClientProxyFactoryImpl implements IClientProxyFactory {
    /**
     * 编码器
     */
    private ICodec codec;
    /**
     * 传输层采用的Client
     */
    private IClient client;
    /**
     * 负载均衡器
     */
    private ILoadBalance loadBalance;

    private final String urlPrefix;


    /**
     * @param codec
     * @param client
     * @param loadBalance
     * @param urlPrefix
     */
    public ClientProxyFactoryImpl(ICodec codec, IClient client, ILoadBalance loadBalance, String urlPrefix) {
        this.codec = Preconditions.checkNotNull(codec);
        this.client = Preconditions.checkNotNull(client);
        this.loadBalance = Preconditions.checkNotNull(loadBalance);
        if (!Strings.isNullOrEmpty(urlPrefix)) {
            this.urlPrefix = urlPrefix;
        } else {
            this.urlPrefix = RpcConstant.RPC_URL_PREFIX_DEFAULT;
        }
    }

    /**
     * 用jdk的动态代理生成代理对象
     * 方法过程：
     * 1.解析被代理对象
     * 2.创建InvocationHandler
     * 3.创建代理类
     *
     * @param target 被代理对象
     * @param <T>
     * @return 代理类
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T newInstance(ServicePoint<T> target) {
        Map<Method, MethodHandler> methodToHandler = parseMethods(target);
        InvocationHandler handler = new RpcInvocationHandler(target, methodToHandler);
        Class[] interfaces = {target.getType()};
        ClassLoader loader = this.getClass().getClassLoader();
        return (T) Proxy.newProxyInstance(loader, interfaces, handler);
    }

    /**
     * /**
     * 暴露给其他类调用的方法
     * 创建被代理类的方法签名,方法handler映射对象
     *
     * @param target 被代理对象
     * @return 方法签名方法handler映射map
     */
    private Map<Method, MethodHandler> parseMethods(ServicePoint target) {
        final Class interfaceType = target.getType();
        Preconditions.checkArgument(interfaceType.isInterface(), "only interface can be proxy,%s", interfaceType);

        Map<Method, MethodHandler> result = Maps.newHashMap();

        Utils.parseMethods(target.getType(), meta -> {
            Preconditions.checkNotNull(meta);
            MethodHandler methodHandler = new MethodHandler(target, codec, client, meta, loadBalance, urlPrefix);
            result.put(meta.getMethod(), methodHandler);
            return null;
        });
        return result;
    }
}

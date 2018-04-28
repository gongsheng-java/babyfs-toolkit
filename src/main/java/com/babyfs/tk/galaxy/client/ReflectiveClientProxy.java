
package com.babyfs.tk.galaxy.client;


import com.babyfs.tk.commons.JavaProxyUtil;
import com.babyfs.tk.galaxy.ProxyUtils;
import com.babyfs.tk.galaxy.codec.IDecoder;
import com.babyfs.tk.galaxy.codec.IEncoder;
import com.babyfs.tk.galaxy.register.ILoadBalance;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

import static com.babyfs.tk.galaxy.ProxyUtils.FORBIDDEN_METHODS;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * ReflectiveClientProxy
 * RpcInvocationHandler内部类用于分发代理对象的method到对应的method handler
 * ParseHandlersByName内部类用于解析代理对象
 */
public class ReflectiveClientProxy implements IClientProxy {

    private final ParseHandlersByName targetToHandlersByName;
    private final IInvocationHandlerFactory factory;

    ReflectiveClientProxy(ParseHandlersByName targetToHandlersByName, IInvocationHandlerFactory factory) {
        this.targetToHandlersByName = checkNotNull(targetToHandlersByName, "targetToHandlersByName");
        this.factory = checkNotNull(factory, "factory");
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
    public <T> T newInstance(ITarget<T> target) {
        Map<String, IInvocationHandlerFactory.IMethodHandler> nameToHandler = targetToHandlersByName.apply(target);
        Map<Method, IInvocationHandlerFactory.IMethodHandler> methodToHandler = new LinkedHashMap<Method, IInvocationHandlerFactory.IMethodHandler>();
        if (!nameToHandler.isEmpty() || methodToHandler.isEmpty())
            for (Method method : target.type().getMethods()) {
                if (FORBIDDEN_METHODS.contains(method)) {
                    continue;
                }
                methodToHandler.put(method, nameToHandler.get(ProxyUtils.configKey(target.type().getSimpleName(), method)));
            }
        InvocationHandler handler = factory.create(target, methodToHandler);
        Class[] interfaces = {target.type()};
        ClassLoader loader = this.getClass().getClassLoader();
        T proxy = (T) Proxy.newProxyInstance(loader, interfaces, handler);
        return proxy;
    }

    /**
     * InvocationHandler实现类
     * 分发代理对象的method到对应的method handler执行
     */
    static class RpcInvocationHandler implements InvocationHandler {

        private final ITarget target;
        private final Map<Method, IInvocationHandlerFactory.IMethodHandler> dispatch;

        RpcInvocationHandler(ITarget target, Map<Method, IInvocationHandlerFactory.IMethodHandler> dispatch) {
            this.target = checkNotNull(target, "target");
            this.dispatch = checkNotNull(dispatch, "dispatch for %s", dispatch);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (dispatch.containsKey(method)) {
                return dispatch.get(method).invoke(args);
            }else {
                return JavaProxyUtil.invokeMethodOfObject(proxy,method,args,new Class[]{target.type()});
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof RpcInvocationHandler) {
                RpcInvocationHandler other = (RpcInvocationHandler) obj;
                return target.equals(other.target);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return target.hashCode();
        }

        @Override
        public String toString() {
            return target.toString();
        }
    }

    /**
     * 类的作用:
     * 1.解析代理对象元数据
     * 2.生成代理对象的方法的method handler
     */
    static final class ParseHandlersByName {

        private final IEncoder encoder;
        private final IDecoder decoder;
        private final MethodHandler.Factory factory;
        private final IClient client;
        private final ILoadBalance loadBalance;
        private final String urlPrefix;

        ParseHandlersByName(IEncoder encoder, IDecoder decoder, IClient client,
                            MethodHandler.Factory factory, ILoadBalance loadBalance, String urlPrefix) {
            this.factory = checkNotNull(factory, "factory");
            this.client = checkNotNull(client, "client");
            this.loadBalance = checkNotNull(loadBalance, "loadBalance");
            this.encoder = checkNotNull(encoder, "encoder");
            this.decoder = checkNotNull(decoder, "decoder");
            this.urlPrefix = checkNotNull(urlPrefix, "String urlPrefix");
        }

        /**
         * 暴露给其他类调用的方法
         * 创建被代理类的方法签名,方法handler映射对象
         *
         * @param key 被代理对象
         * @return 方法签名方法handler映射map
         */
        public Map<String, IInvocationHandlerFactory.IMethodHandler> apply(ITarget key) {

            Map<String, IInvocationHandlerFactory.IMethodHandler> result = new LinkedHashMap<String, IInvocationHandlerFactory.IMethodHandler>();
            List<MethodMetadata> metadata = parseAndValidateMetadata(key.type());
            for (MethodMetadata md : metadata) {
                result.put(md.configKey(),
                        factory.create(key, encoder, decoder, client, md, loadBalance, urlPrefix));
            }
            return result;
        }

        /**
         * 解析并且验证被代理对象的元数据
         *
         * @param targetType
         * @return 被代理对象方法元数据列表
         */
        private List<MethodMetadata> parseAndValidateMetadata(Class<?> targetType) {

            Map<String, MethodMetadata> result = new LinkedHashMap<String, MethodMetadata>();
            for (Method method : targetType.getMethods()) {
                //不处理FORBIDDEN_METHODS
                if (FORBIDDEN_METHODS.contains(method)) {
                    continue;
                }
                MethodMetadata metadata = parseAndValidateMetadata(targetType, method);
                //不容许在一个接口中有两个签名相同的方法
                checkState(!result.containsKey(metadata.configKey()), "Overrides unsupported: %s",
                        metadata.configKey());
                result.put(metadata.configKey(), metadata);
            }
            return new ArrayList(result.values());
        }

        /**
         * 解析方法得到方法签名
         *
         * @param targetType
         * @param method
         * @return
         */
        private MethodMetadata parseAndValidateMetadata(Class<?> targetType, Method method) {
            MethodMetadata data = new MethodMetadata();
            data.configKey(ProxyUtils.configKey(targetType.getSimpleName(), method));
            return data;
        }
    }


}

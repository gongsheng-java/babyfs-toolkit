
package com.babyfs.tk.galaxy.client;


import com.babyfs.tk.galaxy.codec.Decoder;
import com.babyfs.tk.galaxy.codec.Encoder;
import com.babyfs.tk.galaxy.register.LoadBalance;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * GalaxyClientProxy的实现类
 * RpcInvocationHandler内部类用于分发代理对象的method到对应的method handler
 * ParseHandlersByName内部类用于解析代理对象
 */
public class ReflectiveGalaxyClient extends GalaxyClientProxy {

    private final ParseHandlersByName targetToHandlersByName;
    private final IInvocationHandlerFactory factory;

    ReflectiveGalaxyClient(ParseHandlersByName targetToHandlersByName, IInvocationHandlerFactory factory) {
        this.targetToHandlersByName = targetToHandlersByName;
        this.factory = factory;
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
        for (Method method : target.type().getMethods()) {
            methodToHandler.put(method, nameToHandler.get(GalaxyClientProxy.configKey(target.type(), method)));
        }
        InvocationHandler handler = factory.create(target, methodToHandler);
        T proxy = (T) Proxy.newProxyInstance(target.type().getClassLoader(), new Class<?>[]{target.type()}, handler);
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
            this.dispatch = checkNotNull(dispatch, "dispatch for %s", target);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return dispatch.get(method).invoke(args);
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

        private final Encoder encoder;
        private final Decoder decoder;
        private final GalaxyMethodHandler.Factory factory;
        private final IClient client;
        private final LoadBalance loadBalance;

        ParseHandlersByName(Encoder encoder, Decoder decoder, IClient client,
                            GalaxyMethodHandler.Factory factory, LoadBalance loadBalance) {
            this.factory = factory;
            this.client = client;
            this.loadBalance = loadBalance;
            this.encoder = checkNotNull(encoder, "encoder");
            this.decoder = checkNotNull(decoder, "decoder");
        }

        /**
         * ParseHandlersByName暴露给其他类调用的方法
         * 创建被代理类的方法签名方法handler映射对象
         *
         * @param key 被代理对象
         * @return 方法签名方法handler映射map
         */
        public Map<String, IInvocationHandlerFactory.IMethodHandler> apply(ITarget key) {

            Map<String, IInvocationHandlerFactory.IMethodHandler> result = new LinkedHashMap<String, IInvocationHandlerFactory.IMethodHandler>();
            List<MethodMetadata> metadata = parseAndValidatateMetadata(key.type());
            for (MethodMetadata md : metadata) {
                result.put(md.configKey(),
                        factory.create(key, encoder, decoder, client, md, loadBalance));
            }
            return result;
        }

        /**
         * 解析并且验证被代理对象的元数据
         *
         * @param targetType
         * @return 被代理对象方法元数据列表
         */
        private List<MethodMetadata> parseAndValidatateMetadata(Class<?> targetType) {

            checkState(targetType.getTypeParameters().length == 0, "Parameterized types unsupported: %s",
                    targetType.getSimpleName());
            checkState(targetType.getInterfaces().length <= 1, "Only single inheritance supported: %s",
                    targetType.getSimpleName());
            Map<String, MethodMetadata> result = new LinkedHashMap<String, MethodMetadata>();
            for (Method method : targetType.getMethods()) {
                MethodMetadata metadata = parseAndValidateMetadata(targetType, method);
                checkState(!result.containsKey(metadata.configKey()), "Overrides unsupported: %s",
                        metadata.configKey());
                result.put(metadata.configKey(), metadata);
            }
            return new ArrayList(result.values());
        }

        /**
         * 解析方法得到方法元数据
         *
         * @param targetType
         * @param method
         * @return
         */
        private MethodMetadata parseAndValidateMetadata(Class<?> targetType, Method method) {
            MethodMetadata data = new MethodMetadata();
            data.returnType(method.getReturnType());
            data.configKey(GalaxyClientProxy.configKey(targetType, method));
            data.parameterTypes(method.getParameterTypes());
            data.methodName(method.getName());
            return data;
        }

    }


}

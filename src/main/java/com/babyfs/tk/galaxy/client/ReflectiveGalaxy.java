
package com.babyfs.tk.galaxy.client;


import com.babyfs.tk.galaxy.codec.Decoder;
import com.babyfs.tk.galaxy.codec.Encoder;
import com.babyfs.tk.galaxy.register.LoadBalance;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.*;

/**
 * galaxy实现类
 */
public class ReflectiveGalaxy extends Galaxy {

    private final ParseHandlersByName targetToHandlersByName;
    private final InvocationHandlerFactory factory;

    ReflectiveGalaxy(ParseHandlersByName targetToHandlersByName, InvocationHandlerFactory factory) {
        this.targetToHandlersByName = targetToHandlersByName;
        this.factory = factory;
    }

    /**
     * 用jdk的动态代理生成代理对象
     *
     * @param target
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T newInstance(Target<T> target) {
        Map<String, InvocationHandlerFactory.MethodHandler> nameToHandler = targetToHandlersByName.apply(target);
        Map<Method, InvocationHandlerFactory.MethodHandler> methodToHandler = new LinkedHashMap<Method, InvocationHandlerFactory.MethodHandler>();
        List<DefaultMethodHandler> defaultMethodHandlers = new LinkedList<DefaultMethodHandler>();

        for (Method method : target.type().getMethods()) {
            if (method.getDeclaringClass() == Object.class) {
                continue;
            } else if (Util.isDefault(method)) {
                DefaultMethodHandler handler = new DefaultMethodHandler(method);
                defaultMethodHandlers.add(handler);
                methodToHandler.put(method, handler);
            } else {
                methodToHandler.put(method, nameToHandler.get(Galaxy.configKey(target.type(), method)));
            }
        }
        InvocationHandler handler = factory.create(target, methodToHandler);
        T proxy = (T) Proxy.newProxyInstance(target.type().getClassLoader(), new Class<?>[]{target.type()}, handler);

        for (DefaultMethodHandler defaultMethodHandler : defaultMethodHandlers) {
            defaultMethodHandler.bindTo(proxy);
        }
        return proxy;
    }

    /**
     * InvocationHandler实现类
     * 找到method,执行代理方法
     */
    static class RpcInvocationHandler implements InvocationHandler {

        private final Target target;
        private final Map<Method, InvocationHandlerFactory.MethodHandler> dispatch;

        RpcInvocationHandler(Target target, Map<Method, InvocationHandlerFactory.MethodHandler> dispatch) {
            this.target = Util.checkNotNull(target, "target");
            this.dispatch = Util.checkNotNull(dispatch, "dispatch for %s", target);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ("equals".equals(method.getName())) {
                try {
                    Object
                            otherHandler =
                            args.length > 0 && args[0] != null ? Proxy.getInvocationHandler(args[0]) : null;
                    return equals(otherHandler);
                } catch (IllegalArgumentException e) {
                    return false;
                }
            } else if ("hashCode".equals(method.getName())) {
                return hashCode();
            } else if ("toString".equals(method.getName())) {
                return toString();
            }
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
     * 解析代理对象的方法的内部类
     */
    static final class ParseHandlersByName {

        private final Encoder encoder;
        private final Decoder decoder;
        private final SynchronousMethodHandler.Factory factory;
        private final Client client;
        private final LoadBalance loadBalance;

        ParseHandlersByName(Encoder encoder, Decoder decoder, Client client,
                            SynchronousMethodHandler.Factory factory, LoadBalance loadBalance) {
            this.factory = factory;
            this.client = client;
            this.loadBalance = loadBalance;
            this.encoder = Util.checkNotNull(encoder, "encoder");
            this.decoder = Util.checkNotNull(decoder, "decoder");
        }

        /**
         * 得到Map  map key 为方法标识，value 为方法的SynchronousMethodHandler
         *
         * @param key
         * @return
         */
        public Map<String, InvocationHandlerFactory.MethodHandler> apply(Target key) {

            Map<String, InvocationHandlerFactory.MethodHandler> result = new LinkedHashMap<String, InvocationHandlerFactory.MethodHandler>();
            List<MethodMetadata> metadata = parseAndValidatateMetadata(key.type());
            for (MethodMetadata md : metadata) {
                result.put(md.configKey(),
                        factory.create(key, encoder, decoder, client, md, loadBalance));
            }
            return result;
        }

        /**
         * 解析代理接口得到方法元数据列表
         *
         * @param targetType
         * @return
         */
        public List<MethodMetadata> parseAndValidatateMetadata(Class<?> targetType) {
            Util.checkState(targetType.getTypeParameters().length == 0, "Parameterized types unsupported: %s",
                    targetType.getSimpleName());
            Util.checkState(targetType.getInterfaces().length <= 1, "Only single inheritance supported: %s",
                    targetType.getSimpleName());
            if (targetType.getInterfaces().length == 1) {
                Util.checkState(targetType.getInterfaces()[0].getInterfaces().length == 0,
                        "Only single-level inheritance supported: %s",
                        targetType.getSimpleName());
            }
            Map<String, MethodMetadata> result = new LinkedHashMap<String, MethodMetadata>();
            for (Method method : targetType.getMethods()) {
                if (method.getDeclaringClass() == Object.class ||
                        (method.getModifiers() & Modifier.STATIC) != 0 ||
                        Util.isDefault(method)) {
                    continue;
                }
                MethodMetadata metadata = parseAndValidateMetadata(targetType, method);
                Util.checkState(!result.containsKey(metadata.configKey()), "Overrides unsupported: %s",
                        metadata.configKey());
                result.put(metadata.configKey(), metadata);
            }
            return new ArrayList<MethodMetadata>(result.values());
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
            data.returnType(Types.resolve(targetType, targetType, method.getGenericReturnType()));
            data.configKey(Galaxy.configKey(targetType, method));
            data.parameterTypes(method.getParameterTypes());
            data.methodName(method.getName());
            return data;
        }

    }


}

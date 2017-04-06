package com.babyfs.tk.rpc.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.protobuf.Message;
import com.babyfs.tk.rpc.util.ReflectionUtil;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * 基于接口定义的服务封装
 */
public class ServiceWrapper {
    /**
     * 服务的名称
     */
    private final String name;
    /**
     * 被包装的服务对象
     */
    private final Object service;
    /**
     * 禁止代理的方法集合:
     * {@link Object}中的方法都不代理
     */
    protected static final ImmutableSet<Method> FORBIDDEN_METHODS = new ImmutableSet.Builder<Method>().add(Object.class.getMethods()).build();
    /**
     * 服务提供的方法集合
     */
    protected final Map<String, Methods> serviceMethods = Maps.newHashMap();
    /**
     * 服务方法和其id的集合
     */
    protected final Map<Method, String> method2id = Maps.newHashMap();

    /**
     * @param name    服务的名称
     * @param service 被包装的服务对象,这个参数可以是一个{@link Class},当它是一个Class时,
     *                那么这个Class对象应该表示一个接口;这个参数还可以是一个对象的实例,在这种情况下,这个
     *                对象必须至少实现了一个接口
     */
    public ServiceWrapper(String name, Object service) {
        Preconditions.checkArgument(name != null, "The name must be set.");
        Preconditions.checkArgument(service != null, "The service must be set.");
        this.name = name;
        this.service = init(service);
    }

    /**
     * 取得服务的名称
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * 取得服务的实例
     *
     * @return
     */
    public Object getService() {
        return service;
    }

    /**
     * 取得服务的方法
     *
     * @param methodName 方法的名称
     * @param methodId   方法的id
     * @return
     */
    public MethodWrapper getServiceMethod(String methodName, String methodId) {
        Methods methods = this.serviceMethods.get(methodName);
        if (methods == null) {
            throw new IllegalArgumentException("Can't find the method for name [" + methodName + "]");
        }
        MethodWrapper method = methods.getMethod(methodId);
        if (method == null) {
            throw new IllegalArgumentException("Can't find the method for name [" + methodName + "][methodId:" + methodId + "]");
        }
        return method;
    }

    /**
     * 取得方法的签名id
     *
     * @param method
     * @return
     */
    public String getServiceMethodId(Method method) {
        return this.method2id.get(method);
    }


    private Object init(Object service) {
        if (service instanceof Class) {
            //可能是个接口
            Class clazz = (Class) service;
            Preconditions.checkArgument(clazz.isInterface(), "The service shoud be a interface class");
            initInterfaceService(clazz);
            return null;
        } else {
            initInstaceService(service);
            return service;
        }
    }

    /**
     * 初始化由接口定义的服务
     *
     * @param clazz
     */
    private void initInterfaceService(Class clazz) {
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            initMethodWrapper(method);
        }
    }


    /**
     * 初始化由对象实例定义的服务
     *
     * @param service
     */
    private void initInstaceService(Object service) {
        Class clazz = service.getClass();
        /*
            如果是使用Guice interceport增强的class,真正的class是super class
            特别需要注意的是,$EnhancerByGuice$这样的名称是guice internal使用的机制,有可能随着guice的更新而变化
         */
        if (clazz.getName().contains("$EnhancerByGuice$")) {
            clazz = clazz.getSuperclass();
        }
        Class[] interfaces = clazz.getInterfaces();
        Preconditions.checkArgument(interfaces.length > 0, "The service must be implements an interface.");
        Method[] instanceMethods = service.getClass().getMethods();
        //收集所有在接口中定义的方法
        for (Class interfaceClass : interfaces) {
            Method[] methods = interfaceClass.getMethods();
            for (Method method : methods) {
                if (FORBIDDEN_METHODS.contains(method)) {
                    continue;
                } else {
                    for (Method instanceMethod : instanceMethods) {
                        if (ReflectionUtil.sigEquals(instanceMethod, method)) {
                            initMethodWrapper(instanceMethod);
                            break;
                        }
                    }
                }
            }
        }
    }

    private void initMethodWrapper(Method instanceMethod) {
        String instanceMethodName = instanceMethod.getName();
        Methods preMethods = serviceMethods.get(instanceMethodName);
        if (preMethods == null) {
            preMethods = new Methods(instanceMethodName);
            serviceMethods.put(instanceMethodName, preMethods);
        }
        MethodWrapper methodWrapper = new MethodWrapper(instanceMethod);
        preMethods.add(methodWrapper);
        method2id.put(instanceMethod, methodWrapper.methodId);
    }

    /**
     * 相同方法名的方法重载,用于处理方法重载的情况
     */
    private static class Methods {
        /**
         * 方法的名称
         */
        private final String name;
        /**
         * 重载的方法集合
         */
        private final Map<String, MethodWrapper> methodMap = Maps.newHashMap();

        public Methods(String name) {
            Preconditions.checkArgument(name != null, "The name must be set.");
            this.name = name;
        }

        public void add(MethodWrapper methodWrapper) {
            Preconditions.checkArgument(this.name.equals(methodWrapper.method.getName()), "The method name must be %s", this.name);
            if (!methodMap.containsKey(methodWrapper.methodId)) {
                methodMap.put(methodWrapper.methodId, methodWrapper);
            }
        }

        public MethodWrapper getMethod(String methodId) {
            if (methodId != null) {
                return methodMap.get(methodId);
            } else {
                if (methodMap.size() == 1) {
                    //only one method
                    return methodMap.values().iterator().next();
                }
            }
            return null;
        }

    }

    /**
     * 方法的封装
     */
    public static final class MethodWrapper {
        /**
         *
         */
        private final Method method;
        /**
         * 方法的id
         */
        private final String methodId;
        /**
         * 用于辅助创建参数对象的实例,例如Google Proto buffer的编码需要这个利用已知的Message对象创建
         */
        private final Object[] parameterInstanceCreator;

        /**
         * 与{@link #parameterInstanceCreator}类似的用途
         */
        private final Object returnInstanceCreator;


        private MethodWrapper(Method method) {
            this.method = method;
            methodId = ReflectionUtil.methodSignature(method);
            Class<?>[] parameterTypes = method.getParameterTypes();
            parameterInstanceCreator = new Object[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                Class clazz = parameterTypes[i];
                if (Message.class.isAssignableFrom(clazz)) {
                    //特殊处理Google proto buffer的参数
                    parameterInstanceCreator[i] = getProtobufferInstance(clazz);
                }
            }
            Object returnIstance = null;
            Class<?> returnType = method.getReturnType();
            if (Message.class.isAssignableFrom(returnType)) {
                //特殊处理Google proto buffer的参数
                returnIstance = getProtobufferInstance(returnType);
            }
            this.returnInstanceCreator = returnIstance;
        }

        public Method getMethod() {
            return method;
        }

        public String getMethodId() {
            return methodId;
        }

        public Object[] getParameterInstanceCreator() {
            return parameterInstanceCreator;
        }

        public Object getReturnInstanceCreator() {
            return returnInstanceCreator;
        }

        private Object getProtobufferInstance(Class clazz) {
            try {
                Method pbMethod = clazz.getMethod("getDefaultInstance", new Class[0]);
                return pbMethod.invoke(clazz, new Object[0]);
            } catch (Exception e) {
                throw new RuntimeException("Init proto buff instance error", e);
            }
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MethodWrapper)) return false;

            MethodWrapper that = (MethodWrapper) o;

            if (!methodId.equals(that.methodId)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return methodId.hashCode();
        }
    }
}

package com.babyfs.tk.commons;

import com.google.common.base.Joiner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Java Proxy机制的工具类
 */
public final class JavaProxyUtil {
    private static final Method HASH_CODE_METHOD;
    private static final Method EQUALS_METHOD;
    private static final Method TO_STRING_METHOD;

    private JavaProxyUtil() {

    }

    static {
        try {
            HASH_CODE_METHOD = Object.class.getMethod("hashCode");
            EQUALS_METHOD = Object.class.getMethod("equals", new Class[]{Object.class});
            TO_STRING_METHOD = Object.class.getMethod("toString");
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * 代理调用Proxy的{@link Object#hashCode()} {@link Object#equals(Object)} {@link Object#toString()}
     *
     * @param proxy
     * @param method
     * @param args
     * @param interfaces
     * @return
     * @throws InvocationTargetException
     */
    public static Object invokeMethodOfObject(Object proxy, Method method, Object[] args, Class[] interfaces) throws InvocationTargetException {
        if (method.getDeclaringClass() == Object.class) {
            if (method.equals(HASH_CODE_METHOD)) {
                return proxyHashCode(proxy);
            } else if (method.equals(EQUALS_METHOD)) {
                return proxyEquals(proxy, args[0]);
            } else if (method.equals(TO_STRING_METHOD)) {
                return proxyToString(proxy, interfaces);
            }
        }
        throw new InvocationTargetException(new Exception("Unexpected Object method dispatched: " + method + " " + proxyToString(proxy, interfaces)));
    }

    private static Integer proxyHashCode(Object proxy) {
        return Integer.valueOf(System.identityHashCode(proxy));
    }

    private static Boolean proxyEquals(Object proxy, Object other) {
        return (proxy == other ? Boolean.TRUE : Boolean.FALSE);
    }

    private static String proxyToString(Object proxy, Class[] interfaces) {
        String interfaceDesc = "Unknown";
        if (interfaces != null) {
            Joiner on = Joiner.on(",");
            interfaceDesc = on.join(interfaces);
        }
        return proxy.getClass().getName() + '@' + Integer.toHexString(proxy.hashCode()) + " implemented interfaces:" + interfaceDesc;
    }
}

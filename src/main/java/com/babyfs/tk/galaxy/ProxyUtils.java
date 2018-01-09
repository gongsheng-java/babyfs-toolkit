package com.babyfs.tk.galaxy;

import com.google.common.collect.ImmutableSet;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * 代理工具类
 */
public class ProxyUtils {

    /**
     * 禁止代理的方法集合:
     * {@link Object}中的方法都不代理
     */
    public static final ImmutableSet<Method> FORBIDDEN_METHODS = new ImmutableSet.Builder<Method>().add(Object.class.getMethods()).build();

    /**
     * 生成代理对象的方法签名
     *
     * @param className
     * @param method
     * @return
     */
    public static String configKey(String className, Method method) {
        StringBuilder builder = new StringBuilder();
        builder.append(className);
        builder.append('#').append(method.getName()).append('(');
        for (Type param : method.getGenericParameterTypes()) {
            param = method.getReturnType();
            builder.append(param.getTypeName()).append(',');
        }
        if (method.getParameterTypes().length > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }
        return builder.append(')').toString();
    }
}

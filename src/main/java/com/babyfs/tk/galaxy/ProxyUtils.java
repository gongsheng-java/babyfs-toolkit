package com.babyfs.tk.galaxy;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

public class ProxyUtils {

    /**
     * 生成代理对象的方法签名
     *
     * @param targetType
     * @param method
     * @return
     */
    public static String configKey(Class targetType, Method method) {
        StringBuilder builder = new StringBuilder();
        builder.append(targetType.getSimpleName());
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

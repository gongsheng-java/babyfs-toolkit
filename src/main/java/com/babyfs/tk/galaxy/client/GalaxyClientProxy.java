
package com.babyfs.tk.galaxy.client;


import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * 创建客户端被代理对象的抽象类
 * newInstance方法为创建代理对象方法
 * 此类的子类是通过GalaxyClientBuilder创建完成
 */
public abstract class GalaxyClientProxy {

    /**
     * 创建GalaxyClientBuilder方法
     *
     * @return
     */
    public static GalaxyClientProxyBuilder builder() {
        return new GalaxyClientProxyBuilder();
    }

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


    /**
     * 生成代理对象的方法
     *
     * @param target 被代理对象
     * @return 生成的代理对象
     */
    public abstract <T> T newInstance(ITarget<T> target);


}

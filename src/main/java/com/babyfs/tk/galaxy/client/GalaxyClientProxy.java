
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
     * 生成代理对象的方法
     *
     * @param target 被代理对象
     * @return 生成的代理对象
     */
    public abstract <T> T newInstance(ITarget<T> target);


}


package com.babyfs.tk.galaxy.client;


/**
 * 创建客户端被代理对象的抽象类
 * newInstance方法为创建代理对象方法
 * 此类的子类是通过GalaxyClientBuilder创建完成
 */
public abstract class ClientProxy {

    /**
     * 创建GalaxyClientBuilder方法
     *
     * @return
     */
    public static ClientProxyBuilder builder() {
        return new ClientProxyBuilder();
    }

    /**
     * 生成代理对象的方法
     *
     * @param target 被代理对象
     * @return 生成的代理对象
     */
    public abstract <T> T newInstance(ITarget<T> target);


}


package com.babyfs.tk.galaxy.client;


/**
 * rpc client代理接口
 * newInstance方法为创建代理对象方法
 */
public interface IClientProxy {


    /**
     * 生成代理的方法
     *
     * @param target 被代理对象
     * @return 生成的代理对象
     */
    <T> T newInstance(ITarget<T> target);

}

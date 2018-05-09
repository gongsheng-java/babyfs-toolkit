
package com.babyfs.tk.galaxy.client;


import com.babyfs.tk.galaxy.ServicePoint;

/**
 * rpc client代理工厂
 */
public interface IClientProxyFactory {
    /**
     * 生成代理的实例
     *
     * @param target 被代理对象
     * @return 生成的代理对象
     */
    <T> T newInstance(ServicePoint<T> target);
}

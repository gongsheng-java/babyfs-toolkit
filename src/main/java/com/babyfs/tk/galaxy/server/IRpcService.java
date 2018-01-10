package com.babyfs.tk.galaxy.server;

import com.babyfs.tk.commons.model.ServiceResponse;

/**
 * rpc server端执行实际方法的service
 */
public interface IRpcService {

    /**
     * 根据rpc客户端传递的参数在服务端执行该方法
     *
     * @param interfaceName 接口名称
     * @param methodSign    方法签名
     * @param parameters    方法参数
     * @return
     */
    ServiceResponse invoke(String interfaceName, String methodSign, Object[] parameters);
}

package com.babyfs.tk.rpc.service;

import com.google.common.base.Preconditions;

/**
 * 服务端服务注册调用代理
 */
public class ServerServiceProxy extends BaseServiceProxy {

    /**
     * 调用指定的服务
     *
     * @param serviceName 服务名称
     * @param methodName  服务的方法名
     * @param methodId    方法id,用于区分重载的方法
     * @param args        方法调用的参数
     * @return
     * @throws NullPointerException
     * @throws ServiceException
     */
    public Object callService(String serviceName, String methodName, String methodId, Object[] args) {

        ServiceWrapper serviceWrapper = services.get(serviceName);
        Preconditions.checkNotNull(serviceWrapper, "Can't find the service name %s", serviceName);
        Preconditions.checkNotNull(serviceWrapper.getService(), "The service %s is not an instance service.", serviceName);
        ServiceWrapper.MethodWrapper serviceMethod = serviceWrapper.getServiceMethod(methodName, methodId);
        Preconditions.checkNotNull(serviceMethod, "Can't find the service method name:%s,id:%s", serviceName, methodId);

        try {
            return serviceMethod.getMethod().invoke(serviceWrapper.getService(), args);
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

}

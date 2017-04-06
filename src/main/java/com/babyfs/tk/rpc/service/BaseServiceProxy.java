package com.babyfs.tk.rpc.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.MapMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentMap;

/**
 * 服务注册调用代理
 */
public abstract class BaseServiceProxy {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseServiceProxy.class);

    protected final ConcurrentMap<String, ServiceWrapper> services = new MapMaker().makeMap();

    /**
     * 添加一个服务
     *
     * @param serviceName
     * @param service
     */
    public void add(String serviceName, Object service) {
        ServiceWrapper wrapper = new ServiceWrapper(serviceName, service);
        ServiceWrapper pre = services.putIfAbsent(serviceName, wrapper);
        Preconditions.checkState(pre == null, "The service name has already been set for " + pre);
        LOGGER.info("add service[{}] instance[{}]", serviceName, service);
    }

    /**
     * 删除一个服务
     *
     * @param serviceName
     * @return
     */
    public ServiceWrapper remove(String serviceName) {
        return services.remove(serviceName);
    }

    /**
     * 取得服务
     *
     * @param serviceName
     * @return
     */
    public ServiceWrapper get(String serviceName) {
        return this.services.get(serviceName);
    }

    /**
     * 调用指定的服务,找到一个可用的服务即可
     *
     * @param serviceName 服务名称
     * @param methodName  服务的方法名
     * @param methodId    方法id,用于区分重载的方法
     * @param args        方法调用的参数
     * @return
     * @throws NullPointerException
     * @throws ServiceException
     */
    public abstract Object callService(String serviceName, String methodName, String methodId, Object[] args);
}

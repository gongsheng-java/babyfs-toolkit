package com.babyfs.tk.galaxy.register;


import java.util.List;

/**
 * 服务发现接口
 * rpc中负责获取指定appName的服务列表
 */
public interface IDiscoveryClient {
    /**
     * 根据传入的appName获取ServiceInstance列表
     *
     * @param appName
     * @return
     */
    List<ServiceInstance> getInstancesByName(String appName);
}

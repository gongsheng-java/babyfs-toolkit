package com.babyfs.tk.galaxy.register;


import java.util.List;

/**
 * 服务发现接口
 */
public interface DiscoveryClient {

    /**
     * 获取本服务的ServerInstance
     *
     * @return
     */
    ServiceInstance getLocalServiceInstance();

    /**
     * 根据传入的ServerId获取ServiceInstance列表
     *
     * @param serverId
     * @return
     */
    List<ServiceInstance> getInstances(String serverId);

}

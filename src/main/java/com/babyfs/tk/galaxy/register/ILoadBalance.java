package com.babyfs.tk.galaxy.register;

/**
 * 负载均衡器接口
 * 根据规则获取获取应用下的一个服务实例
 */
public interface ILoadBalance {

    /**
     * 根据规则获取获取应用下的一个服务实例
     * @param appName 应用名称
     * @return
     */
    ServiceInstance getServerByAppName(String appName);

    DiscoveryProperties getDiscoveryProperties();
}

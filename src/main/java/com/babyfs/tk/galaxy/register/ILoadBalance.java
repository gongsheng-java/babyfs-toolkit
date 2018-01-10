package com.babyfs.tk.galaxy.register;

/**
 * 负载均衡器接口
 * 根据规则获取获取应用下的一个服务实例
 */
public interface ILoadBalance {

    /**
     * 根据规则获取获取应用的一个服务实例
     *
     * @param appName 应用名称
     * @return
     */
    ServiceInstance getServerByAppName(String appName);

    /**
     * 获取负载均衡器配置文件
     *
     * @return
     */
    IRpcConfigService getDiscoveryProperties();
}

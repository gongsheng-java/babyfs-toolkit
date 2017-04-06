package com.babyfs.tk.commons.name;

/**
 * 命名服务
 */
public interface INameService {
    /**
     * 根据服务名称查询该服务的服务器,具体的策略由实现确认,可以考虑负载均衡等策略
     *
     * @param serviceName 服务的名称
     * @return
     */
    public Server findServerByServiceName(String serviceName);

    /**
     * 根据服务名名称和server id精确超找server
     *
     * @param serviceName 服务的名称
     * @param serverId    服务器的id
     * @return
     */
    Server findServerByServerId(String serviceName, String serverId);
}

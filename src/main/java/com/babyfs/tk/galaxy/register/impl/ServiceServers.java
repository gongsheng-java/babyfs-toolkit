package com.babyfs.tk.galaxy.register.impl;

import com.babyfs.tk.galaxy.register.ServiceServer;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;

/**
 * service对应的服务列表
 */
final class ServiceServers {
    private final String servcieName;
    private final List<ServiceServer> servers = Lists.newCopyOnWriteArrayList();
    private final Set<ServiceServer> uniqServers = Sets.newConcurrentHashSet();

    ServiceServers(String servcieName) {
        this.servcieName = servcieName;
    }

    /**
     * 增加一个server，如果已经存在则不再添加
     *
     * @param server
     */
    public void addServer(ServiceServer server) {
        if (uniqServers.add(server)) {
            servers.add(server);
        }
    }

    /**
     * 删除一个server
     *
     * @param server
     */
    public void removeServer(ServiceServer server) {
        if (uniqServers.remove(server)) {
            servers.remove(server);
        }
    }

    /**
     * 取得所有的server列表
     *
     * @return
     */
    public List<ServiceServer> getServers() {
        return servers;
    }

    @Override
    public String toString() {
        return "ServiceServers{" +
                "servcieName='" + servcieName + '\'' +
                ", servers=" + servers +
                ", uniqServers=" + uniqServers +
                '}';
    }
}

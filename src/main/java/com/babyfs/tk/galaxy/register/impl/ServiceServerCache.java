package com.babyfs.tk.galaxy.register.impl;

import com.babyfs.tk.galaxy.register.ServiceServer;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * service与server的缓存
 */
final class ServiceServerCache {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceServerCache.class);

    Map<ServiceServer, ServiceServer> allServers = Maps.newConcurrentMap();
    Map<String, ServiceServers> serviceServers = Maps.newConcurrentMap();


    synchronized void update(ServiceServer server) {
        if (server == null) {
            return;
        }

        LOGGER.info("update service server:{}", server);

        //对比service的变化
        Set<String> newServcies = server.getServcies();
        final Set<String> oldServcies;
        ServiceServer oldServer = allServers.get(server);
        if (oldServer == null) {
            oldServcies = Collections.emptySet();
        } else {
            oldServcies = oldServer.getServcies();
        }

        Set<String> addServcies = Sets.newHashSet();
        Set<String> removedServices = Sets.newHashSet();

        for (String s : newServcies) {
            if (!oldServcies.contains(s)) {
                addServcies.add(s);
            }
        }

        for (String s : oldServcies) {
            if (!newServcies.contains(s)) {
                removedServices.add(s);
            }
        }

        //处理新增
        for (String addedServcie : addServcies) {
            ServiceServers serviceServers = getOrAddServiceServersIfAbsent(addedServcie);
            serviceServers.addServer(server);
        }

        //处理删除
        for (String removedService : removedServices) {
            ServiceServers serviceServers = getOrAddServiceServersIfAbsent(removedService);
            serviceServers.removeServer(server);
        }
        allServers.put(server, server);
        LOGGER.debug("after update:{}", this);
    }

    synchronized void removeServer(ServiceServer server) {
        if (server == null) {
            return;
        }

        LOGGER.info("remove service server:{}", server);

        //从所有的记录总删除server
        for (Map.Entry<String, ServiceServers> entry : this.serviceServers.entrySet()) {
            ServiceServers servers = entry.getValue();
            servers.removeServer(server);
        }
        allServers.remove(server);
        LOGGER.debug("after remove:{}", this);
    }

    private ServiceServers getOrAddServiceServersIfAbsent(String service) {
        ServiceServers servers = serviceServers.get(service);
        if (servers == null) {
            servers = new ServiceServers(service);
            ServiceServers preServers = serviceServers.putIfAbsent(service, servers);
            if (preServers != null) {
                servers = preServers;
            }
        }
        return servers;
    }

    @Override
    public String toString() {
        return "ServiceServerCache{" +
                "allServers=" + allServers +
                ", serviceServers=" + serviceServers +
                '}';
    }
}

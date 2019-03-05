package com.babyfs.tk.galaxy.register.impl;

import com.babyfs.tk.galaxy.register.ServiceServer;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import javax.xml.ws.Service;
import java.util.*;

/**
 * service对应的服务列表
 */
final class ServiceServers {
    private final String servcieName;
    private ServerGroup serverGroup;
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
        uniqServers.remove(server);
        uniqServers.add(server);
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

    private void groupServers(){
        Map<Long, List<ServiceServer>> map = new HashMap<>();
        Long minVersion = Long.MAX_VALUE;
        //分组
        for (ServiceServer serviceServer :
                uniqServers) {
            long v = Long.MAX_VALUE;
            try{
                v = Long.parseLong(serviceServer.getVersion());
            }catch (Exception e){}

            minVersion = v < minVersion ? v : minVersion;
            List<ServiceServer> serviceServers = map.get(v);
            if(serviceServers == null){
                serviceServers = new LinkedList<>();
                map.put(v, serviceServers);
            }

            serviceServers.add(serviceServer);
        }
        List<ServiceServer> grayList = new ArrayList<>(uniqServers.size());
        grayList.addAll(map.get(minVersion));

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

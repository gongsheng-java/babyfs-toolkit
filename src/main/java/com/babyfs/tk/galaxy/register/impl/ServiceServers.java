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
        groupServers();
    }

    /**
     * 删除一个server
     *
     * @param server
     */
    public void removeServer(ServiceServer server) {
        uniqServers.remove(server);
        groupServers();
    }

    private void groupServers(){
        Map<Long, List<ServiceServer>> map = new HashMap<>();
        Long minVersion = Long.MAX_VALUE;
        ServiceServer[] serviceServersArray = uniqServers.toArray(new ServiceServer[uniqServers.size()]);
        //分组
        for (ServiceServer serviceServer :
                serviceServersArray) {
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
        ArrayList<ServiceServer> grayList = new ArrayList<>(serviceServersArray.length);
        ArrayList<ServiceServer> list = new ArrayList<>(serviceServersArray.length);
        if(map.keySet().size() > 1){
            grayList.addAll(map.get(minVersion));

            for (Long version :
                    map.keySet()) {
                if(version.equals(minVersion)){
                    continue;
                }
                list.addAll(map.get(version));
            }
        }else{
            list.addAll(map.get(minVersion));
        }

        list.trimToSize();
        grayList.trimToSize();

        serverGroup = new ServerGroup(list, grayList);

    }

    /**
     * 取得所有的server列表
     *
     * @return
     */
    public ServerGroup getServers() {
        return serverGroup;
    }

    @Override
    public String toString() {
        return "ServiceServers{" +
                "servcieName='" + servcieName + '\'' +
                ", servers=" + serverGroup +
                ", uniqServers=" + uniqServers +
                '}';
    }
}

package com.babyfs.tk.galaxy.register.impl;

import com.babyfs.tk.galaxy.register.ServiceServer;
import com.google.common.collect.Maps;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * service对应的服务列表
 */
final class ServiceServers {
    private final String servcieName;
    private ServerGroup serverGroup;
    private final Map<ServiceServer, ServiceServer> uniqServers = Maps.newConcurrentMap();

    ServiceServers(String servcieName) {
        this.servcieName = servcieName;
    }

    /**
     * 增加一个server，如果已经存在则不再添加
     *
     * @param server
     */
    public void addServer(final ServiceServer server) {
        AtomicBoolean hasChange = new AtomicBoolean(false);
        uniqServers.compute(server, (serverKey, serverValue) -> {
            if(serverValue == null){
                hasChange.compareAndSet(false, true);
                return server;
            }

            if(!serverValue.getVersion().equalsIgnoreCase(server.getVersion())){
                hasChange.compareAndSet(false, true);
                return server;
            }

            return serverValue;
        });

        if(hasChange.get()){
            groupServers();
        }
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
        ServiceServer[] serviceServersArray = uniqServers.values().toArray(new ServiceServer[uniqServers.size()]);
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
            list.addAll(map.get(minVersion));

            for (Long version :
                    map.keySet()) {
                if(version.equals(minVersion)){
                    continue;
                }
                grayList.addAll(map.get(version));
            }
        }else if(map.keySet().size() == 1){
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

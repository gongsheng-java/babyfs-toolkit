package com.babyfs.tk.galaxy.register;


import java.util.List;

public interface DiscoveryClient {


    ServiceInstance getLocalServiceInstance();

    List<ServiceInstance> getInstances(String serverId) ;


}

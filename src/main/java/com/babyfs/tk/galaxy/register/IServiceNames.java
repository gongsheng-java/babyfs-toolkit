package com.babyfs.tk.galaxy.register;


import com.babyfs.tk.commons.service.ILifeService;
import com.babyfs.tk.galaxy.register.impl.ServerGroup;

import java.util.List;

/**
 * 服务发现接口
 */
public interface IServiceNames extends ILifeService {
    /**
     * 根据服务接口名称获取ServiceInstance列表
     *
     * @param servcieInterfaceName not null or empty
     * @return
     */
    ServerGroup findServers(String servcieInterfaceName);
}

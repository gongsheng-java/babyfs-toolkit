package com.babyfs.tk.galaxy.register.impl;

import com.babyfs.tk.commons.enums.ShutdownOrder;
import com.babyfs.tk.commons.service.LifeServiceSupport;
import com.babyfs.tk.galaxy.register.IServiceNames;
import com.babyfs.tk.galaxy.register.ServiceServer;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;

import java.util.Collections;
import java.util.List;

/**
 * 本地的服务发现客户端
 */
@Order(-2)
@ShutdownOrder(100)
public final class LocalServiceNames extends LifeServiceSupport implements IServiceNames {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalServiceNames.class);

    private final List<ServiceServer> servers;

    /**
     */
    public LocalServiceNames(List<ServiceServer> serviceServers) {
        servers = Collections.unmodifiableList(Lists.newArrayList(serviceServers));
    }

    @Override
    public List<ServiceServer> findServers(String servcieName) {
        return servers;
    }

    /**
     * 启动
     */
    @Override
    protected synchronized void execStart() {

        super.execStart();
    }

    @Override
    protected synchronized void execStop() {
        super.execStop();
    }


}

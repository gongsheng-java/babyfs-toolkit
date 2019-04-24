package com.babyfs.tk.galaxy.register.impl;


import com.babyfs.tk.galaxy.register.ILoadBalance;
import com.babyfs.tk.galaxy.register.IRule;
import com.babyfs.tk.galaxy.register.IServiceNames;
import com.babyfs.tk.galaxy.register.ServiceServer;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import java.util.Set;

/**
 * 负载均衡器
 */
public class LoadBalanceImpl implements ILoadBalance {
    private IServiceNames servcieNames;

    private IRule rule;

    @Inject
    public LoadBalanceImpl(IServiceNames servcieNames, IRule rule) {
        this.servcieNames = Preconditions.checkNotNull(servcieNames);
        this.rule = Preconditions.checkNotNull(rule);
    }

    /**
     * @param servcieName
     * @return
     */
    @Override
    public ServiceServer findServer(String servcieName) {
        ServerGroup servers = servcieNames.findServers(servcieName);
        return rule.choose(servers.getList(), servers.getGrayList());
    }

    @Override
    public ServiceServer findServerAfterFilter(String serviceName, Set<ServiceServer> serviceServerSet) {
        ServerGroup servers = servcieNames.findServers(serviceName);
        return rule.chooseAfterFilter(servers.getList(), servers.getGrayList(), serviceServerSet);
    }
}

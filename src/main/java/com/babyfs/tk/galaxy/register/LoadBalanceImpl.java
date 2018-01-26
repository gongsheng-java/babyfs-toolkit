package com.babyfs.tk.galaxy.register;


import com.babyfs.tk.galaxy.RpcException;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * 负载均衡器
 */
public class LoadBalanceImpl implements ILoadBalance {


    private IDiscoveryClient discoveryClient;

    private IRule rule = new RoundRobinRule();

    public LoadBalanceImpl(IDiscoveryClient discoveryClient, IRule rule) {
        this.discoveryClient = discoveryClient;
        this.rule = rule;
    }

    /**
     * @param appName
     * @return
     */
    public ServiceInstance getServerByAppName(String appName) {
        return rule.choose(discoveryClient.getInstancesByAppName(appName));
    }

}

package com.babyfs.tk.galaxy.register;


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

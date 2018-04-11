package com.babyfs.tk.galaxy.register;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * LoadBalance的构建器
 * 采用ZkDiscoveryClient
 */
public class LoadBalanceBuilder {


    public static LoadBalanceBuilder builder() {
        return new LoadBalanceBuilder();
    }

    private IRule rule = new RoundRobinRule();
    private IDiscoveryClient discoveryClient;

    public LoadBalanceBuilder rule(IRule rule) {
        this.rule = rule;
        return this;
    }

    public LoadBalanceBuilder discoveryClient(IDiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
        return this;
    }

    //构建LoadBalance的方法
    public LoadBalanceImpl build() {
        checkNotNull(rule, "rule");
        checkNotNull(discoveryClient, "discoveryClient");
        return new LoadBalanceImpl(discoveryClient, rule);
    }

}

package com.babyfs.tk.galaxy.register;


import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.KeeperException;

import java.util.concurrent.ExecutionException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 负载均衡器
 * 用builder模式构建负载均衡器对象
 * 默认用ZkDiscoveryClient
 */
public class LoadBalance implements ILoadBalance {

    public static LoadBalance.Builder builder() {
        return new LoadBalance.Builder();
    }

    private DiscoveryClient discoveryClient;

    private IRule rule = new RoundRobinRule();

    public LoadBalance(DiscoveryClient discoveryClient, IRule rule) {
        this.discoveryClient = discoveryClient;
        this.rule = rule;
    }

    /**
     * @param appName
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws KeeperException
     */
    public ServiceInstance getServerByAppName(String appName) {
        return rule.choose(discoveryClient.getInstances(appName));
    }

    public static class Builder {
        private IRule rule = new RoundRobinRule();
        private DiscoveryProperties discoveryProperties;

        public LoadBalance.Builder rule(IRule rule) {
            this.rule = rule;
            return this;
        }

        public LoadBalance.Builder discoveryProperties(DiscoveryProperties discoveryProperties) {
            this.discoveryProperties = discoveryProperties;
            return this;
        }

        public LoadBalance build() {

            checkNotNull(discoveryProperties, "discoveryProperties");
            ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 3);
            CuratorFramework curatorFramework = CuratorFrameworkFactory.builder()
                    .connectString(discoveryProperties.getRegisterUrl())
                    .retryPolicy(retryPolicy)
                    .connectionTimeoutMs(discoveryProperties.getConnectTimeOut())
                    .sessionTimeoutMs(discoveryProperties.getSessionTimeOut())
                    .build();
            curatorFramework.start();
            ZkDiscoveryClient zkDiscoveryClient = new ZkDiscoveryClient(discoveryProperties, curatorFramework);
            zkDiscoveryClient.start();
            return new LoadBalance(zkDiscoveryClient, rule);
        }
    }


}

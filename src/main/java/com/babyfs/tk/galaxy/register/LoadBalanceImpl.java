package com.babyfs.tk.galaxy.register;


import com.babyfs.tk.galaxy.RpcException;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * 负载均衡器
 * 用builder模式构建负载均衡器对象
 * 服务发现客户端默认用ZkDiscoveryClient
 */
public class LoadBalanceImpl implements ILoadBalance {

    public static LoadBalanceImpl.Builder builder() {
        return new LoadBalanceImpl.Builder();
    }

    private IDiscoveryClient discoveryClient;

    private IRpcConfigService discoveryProperties;

    private IRule rule = new RoundRobinRule();

    public LoadBalanceImpl(IDiscoveryClient discoveryClient, IRule rule, IRpcConfigService discoveryProperties) {
        this.discoveryClient = discoveryClient;
        this.rule = rule;
        this.discoveryProperties = discoveryProperties;
    }

    /**
     * @param appName
     * @return
     */
    public ServiceInstance getServerByAppName(String appName) {
        return rule.choose(discoveryClient.getInstancesByAppName(appName));
    }

    @Override
    public IRpcConfigService getRpcProperties() {
        return discoveryProperties;
    }

    //LoadBalance的构建类
    public static class Builder {
        private IRule rule = new RoundRobinRule();
        private IRpcConfigService iRpcConfig;

        public LoadBalanceImpl.Builder rule(IRule rule) {
            this.rule = rule;
            return this;
        }

        public LoadBalanceImpl.Builder discoveryProperties(IRpcConfigService discoveryProperties) {
            this.iRpcConfig = discoveryProperties;
            return this;
        }

        //构建LoadBalance的方法
        public LoadBalanceImpl build(String registerUrl, int connectTimeout, int sessionTimeout) {
            checkNotNull(iRpcConfig, "iRpcConfig");
            checkNotNull(registerUrl, "registerUrl");
            checkState(connectTimeout > 0, "connectTimeout > 0");
            checkState(sessionTimeout > 0, "sessionTimeout > 0");
            /**
             * @param baseSleepTimeMs initial amount of time to wait between retries
             * @param maxRetries max number of times to retry
             */
            ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 3);
            CuratorFramework curatorFramework = CuratorFrameworkFactory.builder()
                    .connectString(registerUrl)
                    .retryPolicy(retryPolicy)
                    .connectionTimeoutMs(connectTimeout)
                    .sessionTimeoutMs(sessionTimeout)
                    .build();
            curatorFramework.start();
            ZkDiscoveryClient zkDiscoveryClient = new ZkDiscoveryClient(iRpcConfig, curatorFramework);
            try {
                zkDiscoveryClient.start();
            } catch (Exception e) {
                throw new RpcException("zkDiscoveryClient start fail", e);
            }
            return new LoadBalanceImpl(zkDiscoveryClient, rule, iRpcConfig);
        }
    }
}

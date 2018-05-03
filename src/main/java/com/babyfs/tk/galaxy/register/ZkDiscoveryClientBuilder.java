package com.babyfs.tk.galaxy.register;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class ZkDiscoveryClientBuilder {

    public static ZkDiscoveryClientBuilder builder() {
        return new ZkDiscoveryClientBuilder();
    }

    private int connectTimeout;
    private String zkRegisterUrl;
    private int sessionTimeout;
    private int port;

    public ZkDiscoveryClientBuilder connectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public ZkDiscoveryClientBuilder sessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
        return this;
    }

    public ZkDiscoveryClientBuilder zkRegisterUrl(String zkRegisterUrl) {
        this.zkRegisterUrl = zkRegisterUrl;
        return this;
    }

    public ZkDiscoveryClientBuilder port(int port) {
        this.port = port;
        return this;
    }

    //构建ZkDiscoveryClient的方法
    public ZkDiscoveryClient build() {
        checkNotNull(zkRegisterUrl, "zkRegisterUrl");
        checkState(connectTimeout > 0, "connectTimeout > 0");
        checkState(sessionTimeout > 0, "sessionTimeout > 0");
        checkState(port > 0, "port > 0");
        /**
         * @param baseSleepTimeMs initial amount of time to wait between retries
         * @param maxRetries max number of times to retry
         */
        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework curatorFramework = CuratorFrameworkFactory.builder()
                .connectString(zkRegisterUrl)
                .retryPolicy(retryPolicy)
                .connectionTimeoutMs(connectTimeout)
                .sessionTimeoutMs(sessionTimeout)
                .build();
        curatorFramework.start();
        ZkDiscoveryClient zkDiscoveryClient = new ZkDiscoveryClient(curatorFramework, appName, port);
        return zkDiscoveryClient;
    }


}

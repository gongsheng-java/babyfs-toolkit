package com.babyfs.tk.galaxy.register;

import com.babyfs.tk.galaxy.demo.DemoDiscoveryProperties;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.KeeperException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;


@Ignore
public class LoadBalanceTest {

    private  Logger logger = LoggerFactory.getLogger(LoadBalanceTest.class);

    private DemoDiscoveryProperties demoDiscoveryProperties = new DemoDiscoveryProperties();

    private CuratorFramework curatorFramework;

    @Before
    public void  before(){

        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 3);
        curatorFramework =  CuratorFrameworkFactory.builder()
                .connectString(demoDiscoveryProperties.getRegisterUrl())
                .retryPolicy(retryPolicy)
                .connectionTimeoutMs(demoDiscoveryProperties.getConnectTimeOut())
                .sessionTimeoutMs(demoDiscoveryProperties.getSessionTimeOut())
                .build();
        curatorFramework.start();
    }

    @Test
    public void  testLoadBalance() throws InterruptedException, ExecutionException, KeeperException {

       String appName = "appName";
       LoadBalance loadBalance =  LoadBalance.builder().build();
       Thread.sleep(2000);
       ServiceInstance serviceInstance =   loadBalance.getServerByAppName(appName);
       logger.error(serviceInstance.getHost());
       Assert.assertTrue(serviceInstance!=null);

    }


}

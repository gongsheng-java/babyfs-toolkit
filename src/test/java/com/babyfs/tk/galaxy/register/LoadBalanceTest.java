package com.babyfs.tk.galaxy.register;

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

    private CuratorFramework curatorFramework;

    @Before
    public void  before(){

        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 3);
        curatorFramework =  CuratorFrameworkFactory.builder()
                .connectString("127.0.0.1:8081")
                .retryPolicy(retryPolicy)
                .connectionTimeoutMs(20000)
                .sessionTimeoutMs(20000)
                .build();
        curatorFramework.start();
    }

    @Test
    public void  testLoadBalance() throws InterruptedException, ExecutionException, KeeperException {

       String appName = "appName";
       LoadBalanceImpl loadBalance =  LoadBalanceImpl.builder().build("127.0.0.1:2181",20000,20000);
       Thread.sleep(2000);
       ServiceInstance serviceInstance =   loadBalance.getServerByAppName(appName);
       logger.error(serviceInstance.getHost());
       Assert.assertTrue(serviceInstance!=null);
    }


}

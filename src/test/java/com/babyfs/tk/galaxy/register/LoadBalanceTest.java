package com.babyfs.tk.galaxy.register;

import com.babyfs.tk.galaxy.demo.DemoApiDiscoveryProperties;
import com.babyfs.tk.galaxy.demo.DemoOpDiscoveryProperty;
import com.babyfs.tk.galaxy.demo.DemoAppNameDiscoveryProperty;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.KeeperException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;


@Ignore
public class LoadBalanceTest {

    private  Logger logger = LoggerFactory.getLogger(LoadBalanceTest.class);

    @Test
    public void  testStartApi() throws InterruptedException, ExecutionException, KeeperException {
       LoadBalanceImpl loadBalance =  LoadBalanceImpl.builder().discoveryProperties(new DemoApiDiscoveryProperties()).build("127.0.0.1:2181",20000,20000);
       Thread.sleep(20000000);
    }


    @Test
    public void  testStartOp() throws InterruptedException, ExecutionException, KeeperException {
        LoadBalanceImpl loadBalance = LoadBalanceImpl.builder().discoveryProperties(new DemoOpDiscoveryProperty()).build("127.0.0.1:2181", 20000, 20000);
        Thread.sleep(20000000);
    }

    @Test
    public void testLoadBalanceRoud(){

        LoadBalanceImpl loadBalance =  LoadBalanceImpl.builder().discoveryProperties(new DemoAppNameDiscoveryProperty()).build("127.0.0.1:2181",20000,20000);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String appName = "api";
        ServiceInstance serviceInstance =   loadBalance.getServerByAppName(appName);
        if(serviceInstance!=null){
            logger.error(serviceInstance.toString());
        }
        else {
            logger.error("oh my god no instance");
        }
    }
}

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

import java.util.UUID;
import java.util.concurrent.ExecutionException;


@Ignore
public class LoadBalanceTest {

    private Logger logger = LoggerFactory.getLogger(LoadBalanceTest.class);
    //zookeeper 出现问题后，application采用缓存中的服务器列表
    // step，启动zookeeper,启动api,停止zookeeper
    @Test
    public void testApi() throws InterruptedException, ExecutionException, KeeperException {
        LoadBalanceImpl loadBalance = LoadBalanceImpl.builder().discoveryProperties(new DemoApiDiscoveryProperties()).build("127.0.0.1:2181", 20000, 20000);
        Thread.sleep(30000);
        while (true) {
            Thread.sleep(30000);
            ServiceInstance serviceInstance = loadBalance.getServerByAppName("api");
            if(serviceInstance!=null) {
                logger.error(serviceInstance.toString());
            }else {
                logger.error("no instance");
            }
            Assert.assertTrue(serviceInstance!=null);
        }
    }
    // zookeeper连接断开,api服务关掉，zookeeper服务从新启动，loadBalance无法获取api serviceInstance;
    /**
     * step1  testApiConnectLost 运行，testOpConnectLost运行，zookeeper停机，testApiConnectLost停机,
     */
    @Test
    public void testOp() throws InterruptedException, ExecutionException, KeeperException {

        LoadBalanceImpl loadBalance = LoadBalanceImpl.builder().discoveryProperties(new DemoOpDiscoveryProperty()).build("127.0.0.1:2181", 20000, 20000);
        Thread.sleep(30000);
        while (true) {
            Thread.sleep(30000);
            ServiceInstance serviceInstance = loadBalance.getServerByAppName("api");
            if(serviceInstance!=null) {
                logger.error(serviceInstance.toString());
            }else {
                logger.error("no provider");
            }
            Assert.assertTrue(serviceInstance==null);
        }
    }



}

package com.babyfs.tk.galaxy.register;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Ignore
public class LoadBalanceTest {

    private Logger logger = LoggerFactory.getLogger(LoadBalanceTest.class);
    //zookeeper 出现问题后，application采用缓存中的服务器列表
    // step，启动zookeeper,启动api,停止zookeeper

    @Test
    public void testApi() throws Exception {

        ZkDiscoveryClient zkDiscoveryClient = ZkDiscoveryClientBuilder.builder().port(9091).appName("api").sessionTimeout(5000).connectTimeout(5000).zkRegisterUrl("127.0.0.1:2181").build();
        zkDiscoveryClient.start();
        LoadBalanceImpl loadBalance = LoadBalanceBuilder.builder().discoveryClient(zkDiscoveryClient).build();
        while (true) {
            Thread.sleep(1000);
            ServiceInstance serviceInstance = loadBalance.getServerByName("api");
            if (serviceInstance != null) {
                logger.info("=======================================================:"+serviceInstance.toString());
            } else {
                logger.error("no instance");
            }
            //Assert.assertTrue(serviceInstance != null);
        }
    }
    // zookeeper连接断开,api服务关掉，zookeeper服务从新启动，loadBalance无法获取api serviceInstance;

    /**
     * step1  testApiConnectLost 运行，testOpConnectLost运行，zookeeper停机，testApiConnectLost停机,
     */

    @Test
    public void testOp() throws Exception {

        ZkDiscoveryClient zkDiscoveryClient = ZkDiscoveryClientBuilder.builder().port(8091).appName("op").sessionTimeout(5000).connectTimeout(5000).zkRegisterUrl("127.0.0.1:2181").build();
        zkDiscoveryClient.start();
        LoadBalanceImpl loadBalance = LoadBalanceBuilder.builder().discoveryClient(zkDiscoveryClient).build();
        Thread.sleep(10000);
        while (true) {
            Thread.sleep(30000);
            ServiceInstance serviceInstance = loadBalance.getServerByName("api");
            if (serviceInstance != null) {
                logger.error(serviceInstance.toString());
            } else {
                logger.error("no provider");
            }
        }
    }


}

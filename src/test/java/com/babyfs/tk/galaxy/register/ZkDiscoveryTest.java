package com.babyfs.tk.galaxy.register;

import com.babyfs.tk.galaxy.demo.DemoDiscoveryProperties;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Ignore
public class ZkDiscoveryTest {


    private Logger logger = LoggerFactory.getLogger("ZkDiscoveryTest");


    private DemoDiscoveryProperties demoDiscoveryProperties = new DemoDiscoveryProperties();

    private CuratorFramework curatorFramework;

    private  ZkDiscoveryClient zkDiscoveryClient ;

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
        zkDiscoveryClient =  new ZkDiscoveryClient(demoDiscoveryProperties,curatorFramework);
    }

    @Test
    public void  registerTest(){

        zkDiscoveryClient.register();
        String path = demoDiscoveryProperties.getDiscoveryPrefix() +"/" + demoDiscoveryProperties.getAppName();
        List<String> list = zkDiscoveryClient.getChildren(path);
        boolean flag = false;
        for(String str: list){
            if(str.equals(demoDiscoveryProperties.getHostname()+":"+ demoDiscoveryProperties.getPort())){
                flag = true;
            }
        }
        Assert.assertTrue(flag);
    }

    @Test
    public void  createNodeTest() throws Exception {

        String path = demoDiscoveryProperties.getDiscoveryPrefix() +"/" + "api" ;
        String node =  "127.0.0.1:8081";
        zkDiscoveryClient.create(path+"/"+node);
        List<String> list = zkDiscoveryClient.getChildren(path);
        boolean flag = false;
        for(String str: list){
            if(str.equals(node)){
                flag = true;
            }
        }
        Assert.assertTrue(flag);
    }

    @Test
    public void  deleteNodeTest() throws Exception {

        String path = demoDiscoveryProperties.getDiscoveryPrefix() +"/" + "test_path" ;
        String node =  "test_delete_node";
        zkDiscoveryClient.create(path+"/" +node);
        zkDiscoveryClient.delete(path+"/" + node);
        List<String> list = zkDiscoveryClient.getChildren(path);
        boolean flag = false;
        for(String str: list){
            if(str.equals(node)){
                flag = true;
            }
        }
        Assert.assertTrue(!flag);
    }



}

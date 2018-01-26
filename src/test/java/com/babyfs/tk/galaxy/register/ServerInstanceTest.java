package com.babyfs.tk.galaxy.register;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerInstanceTest {


    private static  final Logger  LOGGER = LoggerFactory.getLogger(ServerInstanceTest.class);

    @Test
    public  void  deDoubleTest(){

        ServiceInstance serviceInstanceA = new ServiceInstance("api","127.0.0.1",8080);
        ServiceInstance serviceInstanceB = new ServiceInstance("api","127.0.0.1",8080);
        List<ServiceInstance>  list = new CopyOnWriteArrayList<>();
        if(!list.contains(serviceInstanceA)){
            LOGGER.error("no contains A");
            list.add(serviceInstanceA);
        }
        if(!list.contains(serviceInstanceB)){
            list.add(serviceInstanceA);
        }else {
            LOGGER.error("contains B");
        }
        Assert.assertTrue(list.size()==1);
    }
}

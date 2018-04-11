package com.babyfs.tk.galaxy.register;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Ignore
public class RoudRobinTest {

    private Logger logger = LoggerFactory.getLogger("RoudRobinTest");

    @Test
    public void test() {

        RoundRobinRule roundRobinRule = new RoundRobinRule();
        ServiceInstance serviceInstance01 = new ServiceInstance("app1", "10", 8080);
        ServiceInstance serviceInstance02 = new ServiceInstance("app1", "11", 8081);
        ServiceInstance serviceInstance03 = new ServiceInstance("app1", "12", 8082);
        ServiceInstance serviceInstance04 = new ServiceInstance("app1", "13", 8083);
        List<ServiceInstance> serviceInstances = new ArrayList<>();
        serviceInstances.add(serviceInstance01);
        serviceInstances.add(serviceInstance02);
        serviceInstances.add(serviceInstance03);
        serviceInstances.add(serviceInstance04);
        logger.info(roundRobinRule.choose(serviceInstances).getHost());
        logger.info(roundRobinRule.choose(serviceInstances).getHost());
        logger.info(roundRobinRule.choose(serviceInstances).getHost());
        logger.info(roundRobinRule.choose(serviceInstances).getHost());
        logger.info(roundRobinRule.choose(serviceInstances).getHost());
        logger.info(roundRobinRule.choose(serviceInstances).getHost());
        logger.info(roundRobinRule.choose(serviceInstances).getHost());
        logger.info(roundRobinRule.choose(serviceInstances).getHost());
    }
}

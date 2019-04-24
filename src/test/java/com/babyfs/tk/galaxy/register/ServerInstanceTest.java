package com.babyfs.tk.galaxy.register;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerInstanceTest {


    private static final Logger LOGGER = LoggerFactory.getLogger(ServerInstanceTest.class);

    @Test
    public void deDoubleTest() {

        ServiceServer serviceServerA = new ServiceServer(null, "127.0.0.1", 8080, "1000");
        ServiceServer serviceServerB = new ServiceServer(null, "127.0.0.1", 8080, "1000");
        List<ServiceServer> list = new CopyOnWriteArrayList<>();
        if (!list.contains(serviceServerA)) {
            LOGGER.error("no contains A");
            list.add(serviceServerA);
        }
        if (!list.contains(serviceServerB)) {
            list.add(serviceServerA);
        } else {
            LOGGER.error("contains B");
        }
        Assert.assertTrue(list.size() == 1);
    }
}

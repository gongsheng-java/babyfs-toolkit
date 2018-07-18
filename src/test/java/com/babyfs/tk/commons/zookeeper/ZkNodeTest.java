package com.babyfs.tk.commons.zookeeper;

import com.babyfs.tk.service.biz.serialnum.NetUtils;
import com.babyfs.tk.galaxy.Utils;
import com.google.common.base.Splitter;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Ignore
public class ZkNodeTest {

    @Test
    public void addNode() throws Exception {
        String root = "/number";
        CuratorFramework curator = Utils.buildAndStartCurator("127.0.0.1:2181", 5000, 5000);
//        Stat state = curator.checkExists().forPath("/tesssss");
//        System.out.println(JSON.toJSONString(state));
        List<String> children = curator.getChildren().forPath("/serialnum");
        children.forEach(t -> System.out.println(t));
        String test = root + "/test";
        System.out.println(curator.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT_SEQUENTIAL).forPath(test));
    }
    @Test
    public void testIp() throws Exception {
        System.out.println(NetUtils.getLocalAddress().getHostAddress());
    }

    @Test
    public void testForm() {
        int id = Integer.valueOf("1000000001");
        System.out.println(id);
        String test = "/test000001";
        Logger LOGGER = LoggerFactory.getLogger(ZkNodeTest.class);
        LOGGER.error("ssssssssssssss", new RuntimeException("ccccccccc"));
        System.out.println(Splitter.on("/test").splitToList(test).size());
    }

}

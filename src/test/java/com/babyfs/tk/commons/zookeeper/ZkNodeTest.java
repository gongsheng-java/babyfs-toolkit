package com.babyfs.tk.commons.zookeeper;

import com.alibaba.fastjson.JSON;
import com.babyfs.tk.galaxy.Utils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

@Ignore
public class ZkNodeTest {

    @Test
    public void addNode() throws Exception {
        String root = "/number";
        CuratorFramework curator = Utils.buildAndStartCurator("127.0.0.1:2181", 5000, 5000);
        Stat state = curator.checkExists().forPath(root);
        System.out.println(JSON.toJSONString(state));
//        List<String> children = curator.getChildren().forPath(root);
//        children.forEach(t -> System.out.println(t));
        String test = root + "/test";
        curator.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT_SEQUENTIAL).forPath(test);
    }
}

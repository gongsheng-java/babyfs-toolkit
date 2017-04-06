package com.babyfs.tk.commons.name.impl.zookeeper;

import com.babyfs.tk.commons.zookeeper.ZkClient;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.server.ZooKeeperServer;

import java.io.IOException;
import java.util.List;

/**
 */
public class ZkClientTest {
    private ZooKeeperServer zooKeeperServer;

    public static void main(String[] args) throws IOException {
        ZkClient client = new ZkClient("127.0.0.1:2181", 10000, "gsns", "gsns@zookeeper");
        ZooKeeper zooKeeper = client.get(10000);
        getChild(zooKeeper);
        System.out.println("zk:" + zooKeeper);
        int i = 0;
        while (true) {
            zooKeeper = client.get(10000);
            System.out.println("zk" + (i++) + zooKeeper);
            getChild(zooKeeper);
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

    }

    private static void getChild(ZooKeeper zooKeeper) {
        try {
            List<String> children = zooKeeper.getChildren("/", false);
            System.out.println("children:" + children);
        } catch (KeeperException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

}

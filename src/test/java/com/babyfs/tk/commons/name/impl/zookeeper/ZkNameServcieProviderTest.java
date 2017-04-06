package com.babyfs.tk.commons.name.impl.zookeeper;

import com.google.common.base.Function;
import com.babyfs.tk.commons.name.Server;
import com.babyfs.tk.commons.zookeeper.ZkClient;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

/**
 */
public class ZkNameServcieProviderTest {
    public static void main(String[] args) {
        ZkClient zkClient = new ZkClient("10.22.225.66:2181", "gsns", "gsns@zookeeper");
        ZkNameServcieProvider provider = new ZkNameServcieProvider(zkClient, "/services", new ServerNodeJsonCodec());
        provider.init(new Function<List<Server>, Object>() {
            @Override
            public Object apply(@Nullable List<Server> input) {
                System.out.println(input);
                return null;
            }
        });
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

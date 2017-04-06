package com.babyfs.tk.commons.config.internal;

import com.babyfs.tk.commons.config.IConfigService;
import com.babyfs.tk.commons.zookeeper.ZkClient;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Set;

public class ConfigServiceZKImplTest {

    @Test
    @Ignore
    public void testLoad() throws Exception {
        ZkClient zkClient = new ZkClient("zk.kafka.dev.teemo:2181", null, null);
        IConfigService configService = new ConfigServiceZKImpl(zkClient, "/configtest");
        Set<String> keys = configService.keySet();
        Assert.assertNotNull(keys);
        System.out.println("kyes:"+keys);

        Assert.assertNotNull(configService);
        String name2 = configService.get("name2");
        Assert.assertEquals("donyong.wang@email.com", name2);
        Assert.assertEquals("王东永", configService.get("name1"));
        zkClient.close();
    }
}
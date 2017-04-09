package com.babyfs.tk.service.basic.xml;


import com.babyfs.tk.commons.xml.JAXBUtil;
import com.babyfs.tk.service.basic.xml.client.Group;
import com.babyfs.tk.service.basic.xml.client.ServerElement;
import com.babyfs.tk.service.basic.xml.client.ServiceGroup;
import com.babyfs.tk.service.basic.xml.server.Server;
import com.babyfs.tk.service.basic.xml.server.Servers;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;


/**
 * Created by IntelliJ IDEA.
 * To change this template use File | Settings | File Templates.
 */
public class XmlConfigTest {
    @Test
    public void testUnmarshal() {

        ServiceGroup config = JAXBUtil.unmarshal(ServiceGroup.class, "client.xml");
        System.out.println("[bean]--------->" + config.getGroups().get("user.list").getServerList().getServerElements().get(0).getProperties().get("shardCount"));
    }

    @Test
    public void testKestrelConfig() {
        {
            //test for kestrel servers
            Servers kestrelServers = JAXBUtil.unmarshal(Servers.class, "kestrel-servers.xml");
            Assert.assertNotNull(kestrelServers);
            Map<String, Server> servers = kestrelServers.getServers();
            Assert.assertNotNull(servers);
            Assert.assertFalse(servers.isEmpty());
            Server server = servers.get("kestrel_main");
            Assert.assertNotNull(server);
            Assert.assertEquals("10.22.225.66", server.getHost());
        }
        {
            //test for kestrel group
            ServiceGroup serviceGroup = JAXBUtil.unmarshal(ServiceGroup.class, "kestrel-queues.xml");
            assertNotNull(serviceGroup);
            Map<String, Group> groups = serviceGroup.getGroups();
            assertFalse(groups.isEmpty());
            Group msg_queue = groups.get("unit_test_msg");
            Group retweet_queue = groups.get("unit_test_retweet");
            assertNotNull(msg_queue);
            assertNotNull(retweet_queue);
            Group.ServerList serverList = msg_queue.getServerList();
            assertNotNull(serverList);
            List<ServerElement> serverElements = serverList.getServerElements();
            assertNotNull(serverElements);
            assertEquals(1, serverElements.size());
            Map<String, String> properties = msg_queue.getProperties();
            assertNotNull(properties);
            assertFalse(properties.isEmpty());
            String test = properties.get("test");
            assertEquals("3",test);
        }
    }
}

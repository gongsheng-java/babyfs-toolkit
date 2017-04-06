package com.babyfs.tk.commons.name;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.babyfs.tk.commons.event.IEventListener;
import com.babyfs.tk.commons.name.impl.yaml.YamlNameServiceProvider;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 */
public class ServiceRegistryTest {
    @Test
    public void testAdd_Remove_Server() throws Exception {
        ServiceRegistry sr = new ServiceRegistry();
        Server a = new Server("a", "127.0.0.1", 80);
        a.addService("service_1");
        a.addService("service_2");
        Server b = new Server("a", "127.0.0.1", 81);
        b.addService("service_1");
        b.addService("service_2");
        b.addService("service_3");
        sr.addServer(a);
        sr.addServer(b);
        sr.addServer(a);
        sr.addServer(b);

        {
            Server serverA = sr.findServerByServerId("service_1", "a");
            Assert.assertNotNull(serverA);
            Assert.assertEquals(80, serverA.getPort());
            Assert.assertNull(sr.findServerByServerId("service_1", "b"));
        }

        Assert.assertNotNull(sr.findServerByServiceName("service_1"));
        Assert.assertNotNull(sr.findServerByServiceName("service_2"));
        Assert.assertNotNull(sr.findServerByServiceName("service_3"));
        sr.removeServer(b);
        Assert.assertNull(sr.findServerByServiceName("service_3"));
        {

            Server serverA = sr.findServerByServerId("service_1", "a");
            Assert.assertNull(serverA);
        }

        sr.addServer(b);
        Assert.assertNotNull(sr.findServerByServiceName("service_3"));
        b.removeService("service_3");
        sr.addServer(b);
        Assert.assertNotNull(sr.findServerByServiceName("service_3"));
    }

    @Test
    public void testAdd_Remove_Server_With_Provider() throws Exception {
        class YS extends YamlNameServiceProvider {
            public YS(@Nonnull String config) {
                super(config);
            }

            public void fireRemove(final String id) {
                Server toRemoveServer = this.init(new Function<List<Server>, Server>() {
                    @Override
                    public Server apply(@Nullable List<Server> input) {
                        for (Server server : input) {
                            if (server.getId().equals(id)) {
                                return server;
                            }
                        }
                        return null;  //To change body of implemented methods use File | Settings | File Templates.
                    }
                });
                NSProviderEvent event = new NSProviderEvent(NSProviderEventType.DELETE_SERVER, Lists.newArrayList(toRemoveServer));
                for (IEventListener<NSProviderEvent> listener : this.listeners.values()) {
                    listener.onEvent(event);
                }
            }

            public void fireAdd(Server server) {
                NSProviderEvent event = new NSProviderEvent(NSProviderEventType.ADD_SERVER, Lists.newArrayList(server));
                for (IEventListener<NSProviderEvent> listener : this.listeners.values()) {
                    listener.onEvent(event);
                }
            }
        }


        YS ns = new YS("services.yaml");

        ServiceRegistry sr = new ServiceRegistry(ns);

        Assert.assertNotNull(sr.findServerByServiceName("testPB"));
        Assert.assertNotNull(sr.findServerByServiceName("test"));
        Assert.assertNotNull(sr.findServerByServiceName("testABC"));
        Assert.assertNotNull(sr.findServerByServiceName("testBCD"));


        ns.fireRemove("svr_1");
        Assert.assertNull(sr.findServerByServiceName("testABC"));
        Assert.assertNull(sr.findServerByServiceName("testBCD"));

        Server svr_1 = new Server("svr_1", "127.0.0.1", 9002);
        svr_1.addService("testABC");
        svr_1.addService("testBCD");
        svr_1.addService("testDEF");
        ns.fireAdd(svr_1);
        Assert.assertNotNull(sr.findServerByServiceName("testABC"));
        Assert.assertNotNull(sr.findServerByServiceName("testBCD"));
        Assert.assertNotNull(sr.findServerByServiceName("testDEF"));

        ns.reload();
        Assert.assertNotNull(sr.findServerByServiceName("testPB"));
        Assert.assertNotNull(sr.findServerByServiceName("test"));
        Assert.assertNotNull(sr.findServerByServiceName("testABC"));
        Assert.assertNotNull(sr.findServerByServiceName("testBCD"));
        Assert.assertNull(sr.findServerByServiceName("testDEF"));
    }
}

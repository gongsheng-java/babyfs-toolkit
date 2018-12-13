package com.babyfs.tk.service.basic.es;

import com.google.common.collect.Maps;
import com.babyfs.tk.service.basic.es.ESClientFactory;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Map;

/**
 *
 */
public class ESClientFactoryTest {
    @Test
    @Ignore
    public void test_create_transport_client() {
        Map<String, Object> maps = Maps.newHashMap();
        maps.put(ESClientFactory.CLUSTER_NAME, "es-test");
        Client client = ESClientFactory.createTransportClient(maps, "12.0.0.103:9300");
        System.out.println(client);
        Assert.assertNotNull(client);
        client.close();
    }

//    @Test
//    @Ignore
//    public void test_create_node_client() {
//        Map<String, Object> maps = Maps.newHashMap();
//        maps.put(ESClientFactory.CLUSTER_NAME, "es-d0ngw");
//        Node client = ESClientFactory.createNodeClient(maps);
//        Assert.assertNotNull(client);
//        System.out.println(client);
//    }

}

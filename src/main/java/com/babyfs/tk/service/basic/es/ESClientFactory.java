package com.babyfs.tk.service.basic.es;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.elasticsearch.action.admin.cluster.stats.ClusterStatsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.node.Node;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.reflections.util.ConfigurationBuilder.build;

/**
 * ElasticSearch 客户端工厂
 */
public final class ESClientFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ESClientFactory.class);
    /**
     * ElastichSearch 集群名称
     */
    public static final String CLUSTER_NAME = "cluster.name";
    /**
     * Transport Client是否启用嗅探
     */
    public static final String CLIENT_TRANSPORT_SNIFF = "client.transport.sniff";
    /**
     * TCP 端口的值范围
     */
    public static final String TRANSPORT_TCP_PORT = "transport.tcp.port";
    /**
     * 默认的TCP端口值范围,作为客户端,避免与Cluster Node端口重复(默认的端口是9200-9300)
     */
    public static final String TCP_PORT_RANGE = "9310-9399";
    /**
     * 禁用client检查,true:禁用
     */
    public static final String DISABLE_CHECK_CLIENT = "es.client.disable.check";

    static {
        System.setProperty("es.set.netty.runtime.available.processors", "false");
    }


    private ESClientFactory() {
    }

    /**
     * 构建{@link org.elasticsearch.client.transport.TransportClient}
     * TransportClient不加入ElasticSearch Cluster,发起请求时采用轮询机制,这会导致部分请求经过2跳才能完成
     *
     * @param esClusterName ElasticSearch 集群的名称,非空
     * @param esHosts       ElasticSearch 主机列表,主机的格式为host_ip:port,非空
     * @return
     */
    public static Client createTransportClient(String esClusterName, String... esHosts) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(esClusterName), "The es cluster name must not be null");
        Map<String, Object> config = Maps.newHashMap();
        config.put(CLUSTER_NAME, esClusterName);
        return createTransportClient(config, esHosts);
    }

    /**
     * 构建{@link org.elasticsearch.client.transport.TransportClient}
     * TransportClient不加入ElasticSearch Cluster,发起请求时采用轮询机制,这会导致部分请求经过2跳才能完成
     *
     * @param config  配置,其中必须包含的的配置{@link #CLUSTER_NAME},非空
     * @param esHosts ElasticSearch 主机列表,主机的格式为host_ip:port,非空
     * @return
     */
    public static Client createTransportClient(Map<String, Object> config, String... esHosts) {
        Preconditions.checkArgument(esHosts != null, "The es hosts must not be null.");
        Preconditions.checkArgument(esHosts.length > 0, "The es hosts must not be empty.");
        Preconditions.checkArgument(config != null, "The config must not be null");
        Preconditions.checkArgument(config.get(CLUSTER_NAME) != null, "config must contains the key " + CLUSTER_NAME);

        setDefaultConfig(config);
        Settings.Builder settingBuilder = Settings.builder();
        settingBuilder.put(CLIENT_TRANSPORT_SNIFF, true);
        for (Map.Entry<String, Object> configEnt :
                config.entrySet()) {
            settingBuilder.put(configEnt.getKey(), configEnt.getValue().toString());
        }
        
        Settings settings = settingBuilder.build();
        TransportClient client = new PreBuiltTransportClient(settings);
        List<TransportAddress> addresses = Lists.transform(Lists.newArrayList(esHosts), new Function<String, TransportAddress>() {
            @Override
            public TransportAddress apply(@Nonnull String host) {
                Preconditions.checkArgument(!Strings.isNullOrEmpty(host));
                String[] split = host.split(":");
                Preconditions.checkArgument(split.length == 2, "Bad host format %s,the format is host:port", host);
                InetSocketAddress address = new InetSocketAddress(split[0], Integer.parseInt(split[1]));
                return new TransportAddress(address);
            }
        });
        for (TransportAddress address : addresses) {
            client.addTransportAddresses(address);
        }
        return checkClient(client);
    }

//    /**
//     * 构建{@link org.elasticsearch.client.node.NodeClient}
//     * NodeClient会加入ElastichSearch Cluster,发起的请求时可以路由到目标节点,避免"double hop"
//     * 理论上NodeClient比TransportClient效率更高
//     *
//     * @param config 非空
//     * @return {@link org.elasticsearch.client.node.NodeClient}
//     */
//    public static Node createNodeClient(Map<String, Object> config) {
//        Preconditions.checkArgument(config != null, "The config must not be null");
//        Preconditions.checkArgument(config.get(CLUSTER_NAME) != null, "config must contains the key " + CLUSTER_NAME);
//        setDefaultConfig(config);
//        Settings.Builder builder = Settings.builder();
//        for (Map.Entry<String, Object> configEnt :
//                config.entrySet()) {
//            builder.put(configEnt.getKey(), configEnt.getValue().toString());
//        }
//        Node node = NodeBuilder.nodeBuilder().local(false).settings(builder).client(true).node();
//        checkClient(node.client());
//        return node;
//    }

    /**
     * 检查client的状态
     *
     * @param client
     */
    private static Client checkClient(Client client) {
        Preconditions.checkArgument(client != null);
        if ("true".equalsIgnoreCase(System.getProperty(DISABLE_CHECK_CLIENT))) {
            LOGGER.info("Skip es client chekc");
            return client;
        }

        try {
            ClusterStatsResponse clusterStatsNodeResponses = client.admin().cluster().prepareClusterStats().execute().get(3, TimeUnit.SECONDS);
            ClusterHealthStatus status = clusterStatsNodeResponses.getStatus();
            LOGGER.info("ES Cluster status:{}", status);
            Preconditions.checkState(status != null, "Can't fetch the health status");
            return client;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * 设置Client的默认餐宿
     *
     * @param config
     */
    private static void setDefaultConfig(Map<String, Object> config) {
        setIfAbsent(config, TRANSPORT_TCP_PORT, TCP_PORT_RANGE);

    }

    /**
     * @param config
     * @param key
     * @param value
     */
    private static void setIfAbsent(Map<String, Object> config, String key, Object value) {
        Object o = config.get(key);
        if (o == null) {
            config.put(key, value);
        }
    }
}

package com.babyfs.tk.service.basic.es.guice;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.babyfs.tk.commons.service.IContext;
import com.babyfs.tk.service.basic.es.ESClientFactory;
import com.babyfs.tk.service.basic.guice.annotation.ServiceConf;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.lease.Releasable;
import org.elasticsearch.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Elastic Search Client提供者
 */
public class ESClientProvider implements Provider<Client> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ESClientProvider.class);
    public static final String ES_CONF_CLIENT_TYPE = "es.client.type";
    public static final String ES_CONF_CLIENT_HOSTS = "es.client.hosts";
    public static final String TRANSPORT_CLIENT = "TransportClient";

    private static final Map<String, String> ES_UPGRADE_CONVERTER =  new HashMap<String, String>(){
        {
            put("threadpool.index.queue", "thread_pool.index.queue_size");
            put("threadpool.listener.queue", "thread_pool.listener.queue_size");

        }
    };

    private final Map<String, String> conf;

    /**
     * 服务的上下文
     */
    @Inject(optional = true)
    private IContext context;

    @Inject
    public ESClientProvider(@ServiceConf Map<String, String> conf) {
        Preconditions.checkNotNull(conf, "The conf must not be null");
        this.conf = conf;
    }

    @Override
    public Client get() {
        String clientType = conf.get(ES_CONF_CLIENT_TYPE);
        if (clientType == null) {
            LOGGER.info("Use default es client type:{}", clientType);
        }
        LOGGER.info("ES client type:{}", clientType);
        Map<String, Object> map = buildConfigMap(conf);
        Releasable toClose;
        Client client;
        if (clientType.equalsIgnoreCase(TRANSPORT_CLIENT)) {
            String esHosts = conf.get(ES_CONF_CLIENT_HOSTS);

            Preconditions.checkNotNull(esHosts, "The %s must be set for tarnsport client.", ES_CONF_CLIENT_HOSTS);
            client = ESClientFactory.createTransportClient(map, esHosts.split(","));
            toClose = client;
        } else {
            throw new IllegalArgumentException("Can't find the es client tyep:" + clientType);
        }

        final Releasable toCloseClient = toClose;
        if (context != null) {
            LOGGER.info("Add ES client {} to ShutdownActionRegistry", client);
            context.getShutdownActionRegistry().addAction(toCloseClient::close);
        } else {
            LOGGER.warn("Not found ShutdownActionRegistry,the ES client will not be closed when the jvm shutdown.");
        }
        return client;
    }

    private Map<String, Object> buildConfigMap(Map<String, String> conf){
        Map<String, Object> map = Maps.newHashMap();
        map.putAll(conf);
        /**
         * 兼容性移除
         */
        map.remove(ES_CONF_CLIENT_HOSTS);
        map.remove(ES_CONF_CLIENT_TYPE);

        for (Map.Entry<String, String> ent:
        ES_UPGRADE_CONVERTER.entrySet()) {
            Object o = map.get(ent.getKey());
            map.remove(ent.getKey());
            map.put(ent.getValue(), o);
        };
        return map;
    }
}

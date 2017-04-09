package com.babyfs.tk.service.basic.es.guice;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.babyfs.tk.commons.config.IConfigService;
import com.babyfs.tk.commons.guice.GuiceKeys;
import com.babyfs.tk.service.basic.guice.BasicServiceModule;
import com.babyfs.tk.service.basic.guice.annotation.ServiceConf;
import org.elasticsearch.client.Client;

import java.util.Map;
import java.util.Set;

/**
 * 提供ElasticSearch Client
 */
public class ESClientModule extends BasicServiceModule {
    private final String esClientName;
    private final String esClinetConfPrefix;

    /**
     * @param esClientName       es client服务名称,可以空
     * @param esClientConfPrefix es client配置前缀
     */
    public ESClientModule(String esClientName, String esClientConfPrefix) {
        this.esClientName = esClientName;
        this.esClinetConfPrefix = esClientConfPrefix;
    }

    @Override
    protected void configure() {
        Key<Object> confKey = GuiceKeys.getKey(Map.class, ServiceConf.class, String.class, String.class);
        bind(confKey).toProvider(new ESClientConfProvider(this.esClinetConfPrefix));
        super.bindServiceWitchProvider(Client.class, this.esClientName, ESClientProvider.class);
    }

    public static class ESClientConfProvider implements Provider<Map<String, String>> {
        private final String esClinetConfPrefix;
        @Inject
        private IConfigService conf;

        public ESClientConfProvider(String esClinetConfPrefix) {
            this.esClinetConfPrefix = esClinetConfPrefix;
        }

        @Override
        public Map<String, String> get() {
            if (Strings.isNullOrEmpty(this.esClinetConfPrefix)) {
                return conf;
            } else {
                Map<String, String> filterdConf = Maps.newHashMap();
                Set<String> keys = conf.keySet();
                final int length = this.esClinetConfPrefix.length();
                for (String key : keys) {
                    if (key.startsWith(this.esClinetConfPrefix)) {
                        filterdConf.put(key.substring(length), conf.get(key));
                    }
                }
                return filterdConf;
            }
        }
    }
}

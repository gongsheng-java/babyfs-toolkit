package com.babyfs.tk.commons.config.guice;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.name.Names;
import com.babyfs.tk.commons.GlobalKeys;
import com.babyfs.tk.commons.config.ConfigServiceConfig;
import com.babyfs.tk.commons.config.IConfigService;
import com.babyfs.tk.commons.config.IGlobalService;
import com.babyfs.tk.commons.config.internal.*;
import com.babyfs.tk.commons.name.NameConfig;
import com.babyfs.tk.commons.xml.XmlProperties;
import com.babyfs.tk.commons.zookeeper.ZkClient;
import com.babyfs.tk.commons.zookeeper.integration.guice.ZKClientModule;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

/**
 * 配置初始化的Module
 * <p/>
 * 如果初始化配置中有ZkClinet的配置,则会将ZkClient进行绑定,这时如果也使用了{@link ZKClientModule},
 * 会导致注入冲突
 */
public class ConfigServiceModule extends AbstractModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigServiceModule.class);

    private final String initConfigPath;
    private final ImmutableMap<String, String> globalMap;
    private final ZkClient zkClient;
    private final Map<String, IConfigService> configServices;

    /**
     * @param initConfigPath 在classpath中的初始化配置路径
     */
    public ConfigServiceModule(String initConfigPath) {
        this.initConfigPath = Preconditions.checkNotNull(StringUtils.trimToNull(initConfigPath), "The initConfigPath must not be null");
        LOGGER.info("Load global config from {}", initConfigPath);
        Map<String, String> initConfig = Preconditions.checkNotNull(XmlProperties.loadFromXml(initConfigPath), String.format("Can't load init config from %s", initConfigPath));
        globalMap = ImmutableMap.copyOf(initConfig);
        Map<String, String> configMap = Maps.filterKeys(initConfig, new Predicate<String>() {
            @Override
            public boolean apply(@Nullable String input) {
                return input != null && input.startsWith("config.");
            }
        });
        Preconditions.checkState(!configMap.isEmpty(), String.format("No config found from %s", initConfigPath));

        String zkServers = globalMap.get(NameConfig.CONF_NAME_ZK_SERVERS);
        String zkUser = globalMap.get(NameConfig.ZK_AUTH_USER);
        String zkPassword = globalMap.get(NameConfig.ZK_AUTH_PASSWORD);
        if (!Strings.isNullOrEmpty(zkServers)) {
            zkClient = new ZkClient(zkServers, zkUser, zkPassword);
        } else {
            zkClient = null;
        }

        configServices = Maps.newHashMap();
        for (Map.Entry<String, List<IConfigService>> entry : buildConfigService(configMap).entrySet()) {
            String key = entry.getKey().substring("config.".length());
            List<IConfigService> value = entry.getValue();
            IConfigService toBindConfigServie = null;
            if (value.isEmpty()) {
                LOGGER.warn("Not found config for config.{}", key);
                continue;
            }
            if (value.size() > 1) {
                toBindConfigServie = new ChainServiceConfig(value);
            } else {
                toBindConfigServie = value.get(0);
            }
            configServices.put(key, toBindConfigServie);
        }
    }

    @Override
    protected void configure() {
        if (this.zkClient != null) {
            install(new ZKClientModule(new Provider<ZkClient>() {
                @Override
                public ZkClient get() {
                    return zkClient;
                }
            }));
        }
        for (Map.Entry<String, IConfigService> entry : this.configServices.entrySet()) {
            String key = entry.getKey();
            IConfigService value = entry.getValue();
            if (key.isEmpty()) {
                bind(IConfigService.class).toInstance(value);
            } else {
                bind(IConfigService.class).annotatedWith(Names.named(key)).toInstance(value);
            }
        }
        //注册全局配置服务
        bind(IGlobalService.class).to(GlobalServcieImpl.class).asEagerSingleton();
    }

    private Map<String, List<IConfigService>> buildConfigService(Map<String, String> configMap) {
        Map<String, List<IConfigService>> serviceMap = Maps.newHashMap();
        for (Map.Entry<String, String> entry : configMap.entrySet()) {
            final String key = Preconditions.checkNotNull(StringUtils.trimToNull(entry.getKey()));
            final String value = Preconditions.checkNotNull(StringUtils.trimToNull(entry.getValue()));
            final List<ConfigServiceConfig> configServiceConfigs = JSON.parseArray(value, ConfigServiceConfig.class);
            final List<IConfigService> configServiceList = Lists.newArrayList();
            for (ConfigServiceConfig configServiceConfig : configServiceConfigs) {
                final String type = configServiceConfig.getType();
                if (ConfigServiceConfig.TYPE_COMMON_URI.equalsIgnoreCase(type)) {
                    List<String> uris = Preconditions.checkNotNull(configServiceConfig.getUris());
                    List<URI> transform = Lists.transform(uris, new Function<String, URI>() {
                        @Nullable
                        @Override
                        public URI apply(@Nullable String input) {
                            try {
                                return new URI(input);
                            } catch (URISyntaxException e) {
                                throw new IllegalArgumentException("Invalid uri " + input, e);
                            }
                        }
                    });
                    IConfigService configService = new ConfigServiceURIImpl(transform);
                    configServiceList.add(configService);
                } else if (ConfigServiceConfig.TYPE_SYS.equalsIgnoreCase(type)) {
                    IConfigService configService = new ConfigServiceSysPropImpl();
                    configServiceList.add(configService);
                } else if (ConfigServiceConfig.TYPE_ZK.equalsIgnoreCase(type)) {
                    Preconditions.checkNotNull(zkClient, "No zk client for zk config");
                    List<String> uris = Preconditions.checkNotNull(configServiceConfig.getUris(), "Not found uris for zk config.");
                    Preconditions.checkArgument(uris.size() == 1, "Only one uri be allowed for zk config");
                    final String zkUri = uris.get(0);
                    String path = null;
                    try {
                        path = new URI(zkUri).getPath();
                    } catch (URISyntaxException e) {
                        throw new IllegalArgumentException("Invalid uri for zk config " + zkUri, e);
                    }
                    path = Preconditions.checkNotNull(StringUtils.trimToNull(path));
                    IConfigService configService = new ConfigServiceZKImpl(zkClient, path);
                    configServiceList.add(configService);
                } else {
                    throw new IllegalArgumentException("Unsupported type:" + type + ",configURI:" + configServiceConfig);
                }
            }
            serviceMap.put(key, configServiceList);
        }
        return serviceMap;
    }

    public ImmutableMap<String, String> getGlobalMap() {
        return globalMap;
    }
}

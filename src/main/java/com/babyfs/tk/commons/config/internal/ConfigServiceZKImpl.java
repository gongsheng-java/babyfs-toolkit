package com.babyfs.tk.commons.config.internal;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.babyfs.tk.commons.utils.FunctionUtil;
import com.babyfs.tk.commons.zookeeper.ZkClient;
import com.babyfs.tk.commons.zookeeper.ZkMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * 从Zookeeper中加载配置,支持的scheme:
 * <ul>
 * <li>zk</li>
 * </ul>
 */
public class ConfigServiceZKImpl extends BaseConfigService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigServiceZKImpl.class);
    private final ZkMap<String> zkMap;

    /**
     * @param zkClient
     * @param configRoot
     */
    public ConfigServiceZKImpl(ZkClient zkClient, String configRoot) {
        Preconditions.checkNotNull(zkClient, "The zkClient must not be null");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(configRoot) && configRoot.startsWith("/"), "The configRoot must not be null and start with `/`");
        LOGGER.info("Load config from zookeepr {},root path:{}", zkClient.getZkServers(), configRoot);
        zkMap = ZkMap.createZkMap(zkClient, configRoot, new FunctionUtil.ByteArrayToString());
    }

    @Override
    public void load() {
        //Nothing to do
    }

    @Override
    public String get(String key) {
        return zkMap.get(key);
    }

    @Override
    public String get(String key, String defaultValue) {
        String value = get(key);
        return value != null ? value : defaultValue;
    }

    @Override
    public boolean containsKey(String key) {
        return zkMap.containsKey(key);
    }

    @Override
    public Set<String> keySet() {
        return zkMap.keySet();
    }
}

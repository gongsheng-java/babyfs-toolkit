package com.babyfs.tk.commons.config.internal;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.babyfs.tk.commons.xml.XmlProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 从URI加载配置,支持的sheme有:
 * <ul>
 * <li>
 * 空或则null,从classpath中加载
 * </li>
 * <li>
 * {@value #SCHEME_FILE}
 * </li>
 * <li>
 * {@value #SCHEME_HTTP}
 * </li>
 * <li>
 * {@value #SCHEME_HTTPS}
 * </li>
 * </ul>
 */
public class ConfigServiceURIImpl extends BaseConfigService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigServiceURIImpl.class);
    public static final String SCHEME_CLASSPATH = "classpath";
    public static final String SCHEME_FILE = "file";
    public static final String SCHEME_HTTP = "http";
    public static final String SCHEME_HTTPS = "https";

    private static final Set<String> SUPPORTED_SCHEME = Sets.newHashSet(SCHEME_FILE, SCHEME_HTTP, SCHEME_HTTPS);

    private final List<URI> configUris;
    private volatile Map<String, String> map;


    /**
     * @param configUris
     */
    public ConfigServiceURIImpl(List<URI> configUris) {
        this.configUris = Lists.newArrayList(Preconditions.checkNotNull(configUris, "The configUris must not be null"));
        load();
    }

    /**
     * 加载配置
     */
    @Override
    public synchronized void load() {
        Map<String, String> loadedConfig = Maps.newHashMap();
        for (URI uri : this.configUris) {
            Map<String, String> map = Preconditions.checkNotNull(loadFromURI(uri), "Can't load config from uri %s", uri);
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                Preconditions.checkState(!loadedConfig.containsKey(key), "Duplicate key %s from %s", key, uri);
                loadedConfig.put(key, value);
            }
        }
        if (loadedConfig.isEmpty()) {
            LOGGER.warn("Not found any key from configs {}", Joiner.on(",").join(this.configUris));
        }
        this.map = loadedConfig;
    }

    @Override
    public String get(String key) {
        return this.map.get(key);
    }

    @Override
    public String get(String key, String defaultValue) {
        String value = get(key);
        return value != null ? value : defaultValue;
    }

    @Override
    public boolean containsKey(String key) {
        return this.map.containsKey(key);
    }

    @Override
    public Set<String> keySet() {
        return this.map.keySet();
    }

    /**
     * 是否支持指定的scheme
     *
     * @param scheme
     * @return true, 支持;false,不支持
     */
    public static boolean hasScheme(String scheme) {
        return Strings.isNullOrEmpty(scheme) || SUPPORTED_SCHEME.contains(scheme.toLowerCase());
    }

    protected Map<String, String> loadFromURI(URI configUri) {
        String scheme = configUri.getScheme();
        if (Strings.isNullOrEmpty(scheme)) {
            LOGGER.info("Load config from class path {}", configUri);
            return XmlProperties.loadFromXml(configUri.toString());
        } else if (SCHEME_CLASSPATH.equals(scheme)) {
            String path = configUri.getPath();
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            LOGGER.info("Load config from class path {}", path);
            return XmlProperties.loadFromXml(path);
        } else if (SCHEME_FILE.equalsIgnoreCase(scheme) || SCHEME_HTTP.equalsIgnoreCase(scheme) || SCHEME_HTTPS.equalsIgnoreCase(scheme)) {
            try {
                LOGGER.info("Load config from url {}", configUri.toURL());
                return XmlProperties.loadFromXml(configUri.toURL());
            } catch (IOException e) {
                throw new RuntimeException("Load config from url " + configUri + " fail", e);
            }
        }
        throw new UnsupportedOperationException("Unsupported scheme [" + scheme + "]");
    }
}

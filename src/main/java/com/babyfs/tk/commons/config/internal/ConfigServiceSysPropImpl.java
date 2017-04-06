package com.babyfs.tk.commons.config.internal;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * 从{@link System#getProperties()} 和 {@link System#getenv()}中加载数据
 * 加载的顺序为:
 * <ol>
 * <li>{@link System#getenv()}</li>
 * <li>{@link System#getProperties()}</li>
 * </ol>
 * {@link System#getProperties()}会覆盖{@link System#getenv()}的值
 */
public class ConfigServiceSysPropImpl extends BaseConfigService {
    private final Map<String, String> map = Maps.newHashMap();

    public ConfigServiceSysPropImpl() {
        map.putAll(System.getenv());

        Properties properties = System.getProperties();
        Set<Map.Entry<Object, Object>> entries = properties.entrySet();
        for (Map.Entry<Object, Object> entry : entries) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            if (key instanceof String && value != null) {
                map.put((String) key, value.toString());
            }
        }
    }

    @Override
    public void load() {
        //Nothing to do
    }

    @Override
    public String get(String key) {
        return map.get(key);
    }

    @Override
    public String get(String key, String defaultValue) {
        String value = get(key);
        return value != null ? value : defaultValue;
    }

    @Override
    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    @Override
    public Set<String> keySet() {
        return map.keySet();
    }
}

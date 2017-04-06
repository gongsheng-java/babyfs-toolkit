package com.babyfs.tk.commons.config.internal;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Set;

/**
 * 使用Map实现的IConfigService
 */
public class ConfigServiceMapImpl extends BaseConfigService {
    private final Map<String, String> map = Maps.newHashMap();


    public ConfigServiceMapImpl(Map<String, String> map) {
        this.map.putAll(map);
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

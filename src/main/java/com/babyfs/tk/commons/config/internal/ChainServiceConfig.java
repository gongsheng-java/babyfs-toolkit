package com.babyfs.tk.commons.config.internal;

import com.google.common.collect.Sets;
import com.babyfs.tk.commons.config.IConfigService;

import java.util.List;
import java.util.Set;

/**
 * 链式配置
 */
public class ChainServiceConfig extends BaseConfigService {
    private final IConfigService[] configServices;
    private final int size;

    public ChainServiceConfig(List<IConfigService> configServices) {
        this.configServices = new IConfigService[configServices.size()];
        configServices.toArray(this.configServices);
        this.size = this.configServices.length;
    }

    @Override
    public void load() {
        for (int i = 0; i < this.size; i++) {
            configServices[i].load();
        }
    }

    @Override
    public String get(final String key) {
        for (int i = 0; i < this.size; i++) {
            String s = configServices[i].get(key);
            if (s != null) {
                return s;
            }
        }
        return null;
    }

    @Override
    public String get(String key, String defaultValue) {
        String value = get(key);
        return value != null ? value : defaultValue;
    }

    @Override
    public boolean containsKey(final String key) {
        for (int i = 0; i < this.size; i++) {
            if (configServices[i].containsKey(key)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<String> keySet() {
        Set<String> keys = Sets.newHashSet();
        for (int i = 0; i < this.size; i++) {
            keys.addAll(this.configServices[i].keySet());
        }
        return keys;
    }
}

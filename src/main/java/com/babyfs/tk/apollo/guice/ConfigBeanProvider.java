package com.babyfs.tk.apollo.guice;

import com.babyfs.tk.apollo.ConfigLoader;
import com.google.inject.Provider;

public class ConfigBeanProvider implements Provider<Object> {

    private Class<?> configClass;

    public ConfigBeanProvider(Class<?> tClass){
        this.configClass = tClass;

    }

    @Override
    public Object get() {
        return ConfigLoader.getConfig(configClass);
    }
}

package com.babyfs.tk.service.basic.guice;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Provider;

/**
 * 简单的服务配置Module
 */
public class SimpleBasicServiceModule<T> extends BasicServiceModule {
    private final String conf;
    private final Class<T> servcieClass;
    private final Class<? extends Provider<T>> serviceProviderClass;
    private final String serviceName;

    /**
     * @param conf                 配置文件的名称
     * @param servcieClass         服务的类名
     * @param serviceProviderClass 服务的提供者类名
     * @param serviceName          服务名称,可以为空,当为空时不绑定名称
     * @throws IllegalArgumentException
     */
    public SimpleBasicServiceModule(String conf, Class<T> servcieClass, Class<? extends Provider<T>> serviceProviderClass, String serviceName) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(conf), "The conf must not be null or empty.");
        Preconditions.checkArgument(servcieClass != null);
        Preconditions.checkArgument(serviceProviderClass != null);
        this.servcieClass = servcieClass;
        this.serviceProviderClass = serviceProviderClass;
        this.conf = conf;
        this.serviceName = serviceName;
    }

    @Override
    protected void configure() {
        if (this.serviceName == null) {
            bindServiceWithConf(servcieClass, this.conf, serviceProviderClass);
        } else {
            bindNamedServiceWithConf(servcieClass, this.serviceName, this.conf, serviceProviderClass);
        }
    }
}

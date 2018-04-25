package com.babyfs.tk.galaxy.guice;

import com.babyfs.tk.galaxy.client.ClientProxyBuilder;
import com.google.inject.Inject;
import com.google.inject.Injector;

import java.util.function.Function;

/**
 * RpcSuppler
 * function创建代理对象
 */
public class RpcSuppler<T> implements Function<Class<T>, T> {
    private final String appName;
    private Injector injector;
    private Class<T> clazz;

    public RpcSuppler(String appName, Class clazz) {
        this.appName = appName;
        this.clazz = clazz;
    }

    @Inject
    public void setInjector(Injector injector) {
        this.injector = injector;
    }

    @Override
    public T apply(Class<T> aClass) {
        ClientProxyBuilder clientProxyBuilder = injector.getInstance(ClientProxyBuilder.class);
        return clientProxyBuilder.target(clazz, appName);
    }
}

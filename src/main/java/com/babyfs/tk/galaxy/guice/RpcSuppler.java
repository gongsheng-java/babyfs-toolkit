package com.babyfs.tk.galaxy.guice;

import com.babyfs.tk.galaxy.client.GalaxyClientProxy;
import com.babyfs.tk.galaxy.client.GalaxyClientProxyBuilder;
import com.google.inject.Inject;
import com.google.inject.Injector;

import java.util.function.Function;

/**
 * RpcSuppler
 * function创建代理对象
 */
public class RpcSuppler implements Function<Class<?>, Object> {
    private final String appName;
    private Injector injector;
    private GalaxyClientProxyBuilder clientProxyBuilder;

    public RpcSuppler(String appName, GalaxyClientProxyBuilder clientProxyBuilder) {
        this.appName = appName;
        this.clientProxyBuilder = clientProxyBuilder;
    }

    @Inject
    public void setInjector(Injector injector) {
        this.injector = injector;
    }

    @Override
    public Object apply(Class<?> aClass) {
        return GalaxyClientProxy.builder().target(aClass, appName);
    }
}

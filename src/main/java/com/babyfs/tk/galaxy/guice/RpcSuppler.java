package com.babyfs.tk.galaxy.guice;

import com.babyfs.tk.galaxy.client.Galaxy;
import com.google.inject.Inject;
import com.google.inject.Injector;

import java.util.function.Function;

/**
 * 将target加入guice容器的function
 */
public class RpcSuppler implements Function<Class<?>, Object> {
    private final String url;
    private Injector injector;

    public RpcSuppler(String url) {
        this.url = url;
    }

    @Inject
    public void setInjector(Injector injector) {
        this.injector = injector;
    }

    @Override
    public Object apply(Class<?> aClass) {

        return Galaxy.builder().target(aClass, url);
    }
}

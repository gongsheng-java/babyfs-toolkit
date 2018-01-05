package com.babyfs.tk.galaxy.guice;

import com.babyfs.tk.galaxy.guice.ExternalCreationProvider;
import com.babyfs.tk.galaxy.guice.RpcSuppler;
import com.google.inject.PrivateModule;

import java.util.function.Function;

public class InterfaceModule extends PrivateModule {

    private final Function<Class<?>, ?> function;
    private Class aClass;

    InterfaceModule(Function<Class<?>, ?> function,Class aClass) {
        this.function = function;
        this.aClass = aClass;
    }

    @Override
    protected void configure() {
        requestInjection(function);
        bind(aClass).toProvider(new ExternalCreationProvider(function, aClass)).asEagerSingleton();
    }

    public static InterfaceModule build(String appName,Class<?> clazz) {
        return new InterfaceModule(new RpcSuppler(appName,clazz),clazz);
    }
}

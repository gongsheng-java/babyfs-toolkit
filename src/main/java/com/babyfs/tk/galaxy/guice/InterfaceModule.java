package com.babyfs.tk.galaxy.guice;

import com.google.inject.PrivateModule;

import java.util.function.Function;

/**
 * 用于注入需要代理的接口到guice
 */
public class InterfaceModule extends PrivateModule {

    private final Function<Class<?>, ?> function;
    private Class aClass;

    InterfaceModule(Function<Class<?>, ?> function, Class aClass) {
        this.function = function;
        this.aClass = aClass;
    }

    @Override
    protected void configure() {
        //将Injector对象注入function对象
        requestInjection(function);
        bind(aClass).toProvider(new ExternalCreationProvider(function, aClass)).asEagerSingleton();
    }

    /**
     * 代理远程服务的接口，并注入guice
     *
     * @param appName 远程调用的appName
     * @param clazz   代理接口class
     * @return
     */
    public static InterfaceModule build(String appName, Class<?> clazz) {
        return new InterfaceModule(new RpcSuppler(appName, clazz), clazz);
    }
}

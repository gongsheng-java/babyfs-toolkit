package com.babyfs.tk.galaxy.guice;

import com.babyfs.tk.commons.service.ServiceModule;

import java.util.function.Function;

/**
 * 用于注入需要代理的接口到guice
 */
public class InterfaceModule<T> extends ServiceModule {

    private final Function<Class<T>, T> function;
    private Class<T> aClass;

    InterfaceModule(Function<Class<T>, T> function, Class aClass) {
        this.function = function;
        this.aClass = aClass;
    }

    @Override
    protected void configure() {
        //将Injector对象注入function对象
        requestInjection(function);
        bindService(aClass, new ExternalCreationProvider(function, aClass));
    }

    /**
     * 代理远程服务的接口，并注入guice
     *
     * @param appName 远程调用的appName
     * @param clazz   代理接口class
     * @return
     */
    public static <T> InterfaceModule<T> build(String appName, Class<T> clazz) {
        return new InterfaceModule<>(new RpcSuppler<>(appName, clazz), clazz);
    }
}

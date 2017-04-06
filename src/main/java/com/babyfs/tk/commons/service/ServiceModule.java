package com.babyfs.tk.commons.service;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

/**
 * 提供给server使用的服务注册{@link com.google.inject.Module}基类,需要注意的是该模块是一个私有的,
 * 只有通过{@link #bindService(Class, Class)}注册的服务才会被其他的Module使用
 */
public abstract class ServiceModule extends AbstractModule {

    /**
     * @param serviceInterface
     * @param serviceImpl
     * @param <T>
     */
    protected synchronized <T> void bindService(Class<T> serviceInterface, Class<? extends T> serviceImpl) {
        bind(serviceInterface).to(serviceImpl).asEagerSingleton();
        Key<T> key = Key.get(serviceInterface);

        Multibinder<ServiceEnrty> multibinder = Multibinder.newSetBinder(binder(), ServiceEnrty.class);
        multibinder.addBinding().toInstance(new ServiceEnrty(serviceInterface.getSimpleName(), serviceInterface.getName(), key, null));
    }


    /**
     * @param serviceInterface
     * @param serviceImpl
     * @param name
     * @param <T>
     */
    protected synchronized <T> void bindService(Class<T> serviceInterface, Class<? extends T> serviceImpl, String name) {
        Named named = Names.named(name);
        bind(serviceInterface).annotatedWith(named).to(serviceImpl).asEagerSingleton();
        Key<T> key = Key.get(serviceInterface, named);

        Multibinder<ServiceEnrty> multibinder = Multibinder.newSetBinder(binder(), ServiceEnrty.class);
        multibinder.addBinding().toInstance(new ServiceEnrty(name, serviceInterface.getName(), key, name));
    }

    /**
     * @param serviceInterface
     * @param serviceInstance  服务实例
     * @param <T>
     */
    protected synchronized <T> void bindService(Class<T> serviceInterface, T serviceInstance) {
        bind(serviceInterface).toInstance(serviceInstance);
        Key<T> key = Key.get(serviceInterface);

        Multibinder<ServiceEnrty> multibinder = Multibinder.newSetBinder(binder(), ServiceEnrty.class);
        multibinder.addBinding().toInstance(new ServiceEnrty(serviceInterface.getSimpleName(), serviceInterface.getName(), key, null));
    }

    /**
     * @param serviceInterface
     * @param serviceInstance  服务实例
     * @param name
     * @param <T>
     */
    protected synchronized <T> void bindService(Class<T> serviceInterface, T serviceInstance, String name) {
        Named named = Names.named(name);
        bind(serviceInterface).annotatedWith(named).toInstance(serviceInstance);
        Key<T> key = Key.get(serviceInterface, named);

        Multibinder<ServiceEnrty> multibinder = Multibinder.newSetBinder(binder(), ServiceEnrty.class);
        multibinder.addBinding().toInstance(new ServiceEnrty(name, serviceInterface.getName(), key, name));
    }

    /**
     * @param serviceInterface
     * @param provider         服务实例
     * @param <T>
     */
    protected synchronized <T> void bindService(Class<T> serviceInterface, Provider<? extends T> provider) {
        bind(serviceInterface).toProvider(provider).asEagerSingleton();
        Key<T> key = Key.get(serviceInterface);

        Multibinder<ServiceEnrty> multibinder = Multibinder.newSetBinder(binder(), ServiceEnrty.class);
        multibinder.addBinding().toInstance(new ServiceEnrty(serviceInterface.getSimpleName(), serviceInterface.getName(), key, null));
    }

    /**
     * @param serviceInterface
     * @param provider         服务实例
     * @param <T>
     */
    protected synchronized <T> void bindService(Class<T> serviceInterface, Key<Provider<? extends T>> provider) {
        bind(serviceInterface).toProvider(provider).asEagerSingleton();
        Key<T> key = Key.get(serviceInterface);

        Multibinder<ServiceEnrty> multibinder = Multibinder.newSetBinder(binder(), ServiceEnrty.class);
        multibinder.addBinding().toInstance(new ServiceEnrty(serviceInterface.getSimpleName(), serviceInterface.getName(), key, null));
    }

    /**
     * @param serviceInterface
     * @param provider
     * @param name
     * @param <T>
     */
    protected synchronized <T> void bindService(Class<T> serviceInterface, Provider<? extends T> provider, String name) {
        Named named = Names.named(name);
        bind(serviceInterface).annotatedWith(named).toProvider(provider).asEagerSingleton();
        Key<T> key = Key.get(serviceInterface, named);

        Multibinder<ServiceEnrty> multibinder = Multibinder.newSetBinder(binder(), ServiceEnrty.class);
        multibinder.addBinding().toInstance(new ServiceEnrty(name, serviceInterface.getName(), key, name));
    }

    /**
     * @param serviceInterface
     * @param provider
     * @param name
     * @param <T>
     */
    protected synchronized <T> void bindService(Class<T> serviceInterface, Key<Provider<? extends T>> provider, String name) {
        Named named = Names.named(name);
        bind(serviceInterface).annotatedWith(named).toProvider(provider).asEagerSingleton();
        Key<T> key = Key.get(serviceInterface, named);

        Multibinder<ServiceEnrty> multibinder = Multibinder.newSetBinder(binder(), ServiceEnrty.class);
        multibinder.addBinding().toInstance(new ServiceEnrty(name, serviceInterface.getName(), key, name));
    }

    /**
     * @param serviceInterface
     * @param provider
     * @param <T>
     */
    protected synchronized <T> void bindServiceWithProvider(Class<T> serviceInterface, Class<? extends Provider<? extends T>> provider) {
        bind(serviceInterface).toProvider(provider).asEagerSingleton();
        Key<T> key = Key.get(serviceInterface);

        Multibinder<ServiceEnrty> multibinder = Multibinder.newSetBinder(binder(), ServiceEnrty.class);
        multibinder.addBinding().toInstance(new ServiceEnrty(serviceInterface.getSimpleName(), serviceInterface.getName(), key, null));
    }

    /**
     * @param serviceInterface
     * @param provider
     * @param name
     * @param <T>
     */
    protected synchronized <T> void bindServiceWithProvider(Class<T> serviceInterface, Class<? extends Provider<? extends T>> provider, String name) {
        Named named = Names.named(name);
        bind(serviceInterface).annotatedWith(named).toProvider(provider).asEagerSingleton();
        Key<T> key = Key.get(serviceInterface, named);

        Multibinder<ServiceEnrty> multibinder = Multibinder.newSetBinder(binder(), ServiceEnrty.class);
        multibinder.addBinding().toInstance(new ServiceEnrty(name, serviceInterface.getName(), key, name));
    }

    /**
     * 将已经绑定的类型添加到{@link ServiceEnrty}中
     *
     * @param serviceInterface 服务的类型
     * @param <T>
     */
    protected synchronized <T> void addBindedService(Class<T> serviceInterface) {
        addBindedService(serviceInterface, Key.get(serviceInterface));
    }

    /**
     * 将已经绑定的类型添加到{@link ServiceEnrty}中
     *
     * @param serviceInterface 服务的类型
     * @param key              服务所绑定的key
     * @param <T>
     */
    protected synchronized <T> void addBindedService(Class<T> serviceInterface, Key<T> key) {
        requireBinding(key);
        Multibinder<ServiceEnrty> multibinder = Multibinder.newSetBinder(binder(), ServiceEnrty.class);
        multibinder.addBinding().toInstance(new ServiceEnrty(serviceInterface.getSimpleName(), serviceInterface.getName(), key, null));
    }
}

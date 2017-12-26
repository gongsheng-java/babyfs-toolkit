package com.babyfs.tk.galaxy.guice;

import com.google.inject.Provider;

import java.util.function.Function;

/**
 * guice provider
 * 目的代理接口加入guice容器
 *
 * @param <T>
 */
public class ExternalCreationProvider<T> implements Provider<T> {
    private final Function<Class<?>, ?> supplier;
    private final Class<? super T> clazz;

    public ExternalCreationProvider(Function<Class<?>, ?> supplier, Class<? super T> clazz) {
        this.supplier = supplier;
        this.clazz = clazz;
    }

    @Override
    public T get() {
        Object item = supplier.apply(clazz);
        return (T) clazz.cast(item);
    }
}

package com.babyfs.tk.commons.service;


import com.google.inject.Injector;

/**
 * 对GuiceInject的封装,用于将{@link com.google.inject.Injector}注入到Spring中
 */
public final class GuiceInjector {
    public static final String GUICE_INJECTOR_BEAN_NAME = "GUICE.INJECTOR";
    private final Injector injector;

    public GuiceInjector(Injector injector) {
        this.injector = injector;
    }

    public Injector getInjector() {
        return injector;
    }
}

package com.babyfs.tk.galaxy.guice;

import com.babyfs.tk.galaxy.server.IMethodCacheService;
import com.babyfs.tk.galaxy.server.impl.MethodCacheServiceImpl;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.PrivateModule;
import com.google.inject.Provider;

public class MethodCacheServiceModel extends PrivateModule {


    @Override
    protected void configure() {
        bind(IMethodCacheService.class).toProvider(MethodCacheServiceModel.MethodCacheServiceProvider.class).asEagerSingleton();
        expose(IMethodCacheService.class);
    }

    private static class MethodCacheServiceProvider implements Provider<MethodCacheServiceImpl> {

        @Inject
        private Injector injector;

        @Override
        public MethodCacheServiceImpl get() {
            MethodCacheServiceImpl methodCacheService = new MethodCacheServiceImpl(injector);
            methodCacheService.init();
            return methodCacheService;
        }
    }
}

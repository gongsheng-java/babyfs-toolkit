package com.babyfs.tk.service.biz.counter.guice;

import com.babyfs.tk.commons.service.ServiceModule;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 */
public class CounterSyncServiceModule extends ServiceModule {
    private final Class<? extends SyncServcieRegistry> clazz;

    public CounterSyncServiceModule(Class<? extends SyncServcieRegistry> clazz) {
        this.clazz = Preconditions.checkNotNull(clazz);
    }

    @Override
    protected void configure() {
        bind(SyncServcieRegistry.class).to(this.clazz).asEagerSingleton();
    }
}

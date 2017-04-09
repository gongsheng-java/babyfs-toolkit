package com.babyfs.tk.service.biz.pubsub.guice;

import com.babyfs.tk.commons.application.LifeServiceBindUtil;
import com.babyfs.tk.commons.service.ServiceModule;
import com.babyfs.tk.service.biz.pubsub.RedisPubSubServcie;
import com.google.inject.Singleton;

/**
 * 订阅
 */
public class PubSubModule extends ServiceModule {
    public PubSubModule() {
    }


    @Override
    protected void configure() {
        bind(RedisPubSubServcie.class).in(Singleton.class);
        LifeServiceBindUtil.addLifeService(this.binder(), RedisPubSubServcie.class);
    }
}

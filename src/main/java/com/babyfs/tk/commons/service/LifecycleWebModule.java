package com.babyfs.tk.commons.service;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.babyfs.tk.commons.application.LifeServiceBindUtil;
import com.babyfs.tk.commons.service.annotation.AfterStartStage;
import com.babyfs.tk.commons.service.annotation.InitStage;
import com.babyfs.tk.commons.service.annotation.ShutdownStage;
import com.babyfs.tk.commons.service.internal.*;

/**
 * 服务生命周期的Module,提供如下的服务的是绑定:
 * 1. 服务在启动和停止阶段的动作注册机制:
 * a. {@link InitStage}
 * b. {@link ShutdownStage}
 * 分别绑定到{@link StageActionRegistrySupport}
 * 2. {@link IContext}
 * <p/>
 */
public class LifecycleWebModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new VersionModule());
        bind(IStageActionRegistry.class).annotatedWith(InitStage.class).to(InitStageRegistry.class).asEagerSingleton();
        bind(IStageActionRegistry.class).annotatedWith(AfterStartStage.class).to(AfterStartStageRegistry.class).asEagerSingleton();
        bind(IStageActionRegistry.class).annotatedWith(ShutdownStage.class).to(ShutdownStageRegistry.class).asEagerSingleton();
        bind(IContext.class).to(ContextImpl.class).asEagerSingleton();
        LifeServiceBindUtil.initLifeService(binder());
    }

    /**
     * 初始化{@link IContext}中的injector,这个方法需要在应用启动前调用
     *
     * @param injector
     */
    public static void initServiceContext(final Injector injector) {
        injector.getInstance(IContext.class).setInjector(injector);
    }
}

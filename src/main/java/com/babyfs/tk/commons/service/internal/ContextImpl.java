package com.babyfs.tk.commons.service.internal;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import com.babyfs.tk.commons.GlobalKeys;
import com.babyfs.tk.commons.service.IContext;
import com.babyfs.tk.commons.service.IStageActionRegistry;
import com.babyfs.tk.commons.service.annotation.AfterStartStage;
import com.babyfs.tk.commons.service.annotation.InitStage;
import com.babyfs.tk.commons.service.annotation.ShutdownStage;

/**
 */
public class ContextImpl implements IContext {
    @Inject(optional = true)
    @Named(GlobalKeys.APP_GLOBAL_ARG_KEY_NAME)
    private ImmutableMap<String, String> appArg;


    @Inject
    @InitStage
    private IStageActionRegistry start;

    @Inject(optional = true)
    @AfterStartStage
    private IStageActionRegistry after;

    @Inject
    @ShutdownStage
    private IStageActionRegistry shutdown;

    private Injector injector;

    @Override
    public ImmutableMap<String, String> getAppArg() {
        return appArg;
    }

    @Override
    public IStageActionRegistry getInitActionRegistry() {
        return start;
    }

    @Override
    public IStageActionRegistry getAfterActionRegistry() {
        return after;
    }

    @Override
    public IStageActionRegistry getShutdownActionRegistry() {
        return shutdown;
    }

    @Override
    public Injector getInjector() {
        return this.injector;
    }

    public void setInjector(Injector injector) {
        this.injector = injector;
    }
}

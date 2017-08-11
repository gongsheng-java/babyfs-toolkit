package com.babyfs.tk.commons.application;

import com.babyfs.tk.commons.service.ILifeService;
import com.babyfs.tk.commons.service.annotation.InitStage;
import com.babyfs.tk.commons.service.annotation.LifecycleServiceRegistry;
import com.google.common.base.Preconditions;
import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.multibindings.Multibinder;

import javax.annotation.Nonnull;

/**
 * {@link ILifeService}的绑定工具,使用该类添加绑定后,在需要注入的地方:
 * <code>
 *
 * @Inject
 * @LifecycleServiceRegistry private Set<ILifeService> lifeServices;
 * </code>
 */
public final class LifeServiceBindUtil {
    private LifeServiceBindUtil() {

    }

    /**
     * 向Guice中注册绑定一个{@link ILifeService}的实例,经过这里注册的servcie,将在{@link InitStage}阶段后,
     * 由{@link ApplicationSupport}负责依次启动
     *
     * @param binder
     * @param lifeService
     */
    public static void addLifeService(@Nonnull Binder binder, @Nonnull ILifeService lifeService) {
        Preconditions.checkNotNull(binder, "binder");
        Preconditions.checkNotNull(lifeService, "service");
        Multibinder<ILifeService> multibinder = Multibinder.newSetBinder(binder, ILifeService.class, LifecycleServiceRegistry.class);
        multibinder.addBinding().toInstance(lifeService);
    }

    /**
     * 向Guice中注册绑定一个{@link ILifeService}的实例,经过这里注册的servcie,将在{@link InitStage}阶段后,
     * 由{@link ApplicationSupport}负责依次启动
     *
     * @param binder
     * @param lifeServiceClass
     */
    public static void addLifeService(@Nonnull Binder binder, @Nonnull Class<? extends ILifeService> lifeServiceClass) {
        Preconditions.checkNotNull(binder, "binder");
        Preconditions.checkNotNull(lifeServiceClass, "service");
        Multibinder<ILifeService> multibinder = Multibinder.newSetBinder(binder, ILifeService.class, LifecycleServiceRegistry.class);
        multibinder.addBinding().to(lifeServiceClass).asEagerSingleton();
    }

    /**
     * @param binder
     * @param lifeServiceKey
     */
    public static void addLifeService(@Nonnull Binder binder, @Nonnull Key<? extends ILifeService> lifeServiceKey) {
        Preconditions.checkNotNull(binder, "binder");
        Preconditions.checkNotNull(lifeServiceKey, "lifeServiceKey");
        Multibinder<ILifeService> multibinder = Multibinder.newSetBinder(binder, ILifeService.class, LifecycleServiceRegistry.class);
        multibinder.addBinding().to(lifeServiceKey).asEagerSingleton();
    }

    /**
     * @param binder
     * @param provider
     */
    public static void addLifeServiceWithProvider(@Nonnull Binder binder, @Nonnull Class<? extends Provider<? extends ILifeService>> provider) {
        Preconditions.checkNotNull(binder, "binder");
        Multibinder<ILifeService> multibinder = Multibinder.newSetBinder(binder, ILifeService.class, LifecycleServiceRegistry.class);
        multibinder.addBinding().toProvider(provider).asEagerSingleton();
    }

    /**
     * 初始化一个空的Multibinder
     *
     * @param binder
     */
    public static void initLifeService(@Nonnull Binder binder) {
        Preconditions.checkNotNull(binder, "binder");
        Multibinder.newSetBinder(binder, ILifeService.class, LifecycleServiceRegistry.class);
    }
}

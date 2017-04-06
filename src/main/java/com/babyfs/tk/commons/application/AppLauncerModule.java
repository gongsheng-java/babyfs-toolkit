package com.babyfs.tk.commons.application;

import com.babyfs.tk.commons.service.IStageActionRegistry;
import com.babyfs.tk.commons.service.LifecycleModule;
import com.babyfs.tk.commons.service.annotation.InitStage;
import com.babyfs.tk.commons.service.annotation.ShutdownStage;
import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.name.Names;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AppLauncher的Module
 */
public class AppLauncerModule extends AbstractModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppLauncerModule.class);
    public static final String[] DEFAULT_ARGS = new String[0];
    private final IApplication application;

    public AppLauncerModule(IApplication application) {
        Preconditions.checkArgument(application != null, "application");
        this.application = application;
    }

    @Override
    protected void configure() {
        install(new LifecycleModule());
        Iterable<? extends Module> applicationModules = application.getModules();
        for (Module module : applicationModules) {
            install(module);
        }
        String[] args = application.getArgs();
        if (args == null) {
            LOGGER.warn("no args supplied,use default args");
            args = DEFAULT_ARGS;
        }
        bind(String[].class).annotatedWith(Names.named(IApplication.BIND_APP_ARGS_NAME)).toInstance(args);
        bind(IApplication.class).toInstance(application);
        bind(Thread.UncaughtExceptionHandler.class).toInstance(new AppUncaughtExceptionHandler());
        LifeServiceBindUtil.initLifeService(binder());
        requestStaticInjection(StageInit.class);
    }

    public static final class StageInit {
        private StageInit() {

        }

        /**
         * 注册启动动作:安装线程的异常处理器
         *
         * @param startupRegistry
         * @param uncaughtExceptionHandler
         */
        @Inject
        public static void applyUncaughtExceptionHandler(@InitStage IStageActionRegistry startupRegistry, final Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
            startupRegistry.addAction(new ExceptionRunnable(uncaughtExceptionHandler));
        }

        /**
         * 注册jvm停机的动作:停止Application
         *
         * @param shutdownRegistry
         * @param application
         */
        @Inject
        public static void shutdown(@ShutdownStage final IStageActionRegistry shutdownRegistry, final IApplication application) {
            shutdownRegistry.addAction(new AppShutdownRunnable(application));
        }

        private static class ExceptionRunnable implements Runnable {
            private final Thread.UncaughtExceptionHandler uncaughtExceptionHandler;

            public ExceptionRunnable(Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
                this.uncaughtExceptionHandler = uncaughtExceptionHandler;
            }

            @Override
            public void run() {
                Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler);
            }
        }

        private static class AppShutdownRunnable implements Runnable {
            private final IApplication application;

            public AppShutdownRunnable(IApplication application) {
                this.application = application;
            }

            @Override
            public void run() {
                application.stopAsync().awaitTerminated();
            }
        }
    }

    private static class AppUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            LOGGER.error("FATAL,Uncaugth exception form therad " + t, e);
        }
    }
}

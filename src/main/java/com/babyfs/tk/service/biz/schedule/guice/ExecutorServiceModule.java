package com.babyfs.tk.service.biz.schedule.guice;

import com.babyfs.tk.commons.config.IConfigService;
import com.babyfs.tk.commons.service.IStageActionRegistry;
import com.babyfs.tk.commons.service.LifecycleModule;
import com.babyfs.tk.commons.service.annotation.ShutdownStage;
import com.babyfs.tk.commons.thread.NamedThreadFactory;
import com.babyfs.tk.commons.utils.MapUtil;
import com.babyfs.tk.commons.utils.ThreadUtil;
import com.babyfs.tk.service.biz.schedule.IScheduleService;
import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Names;

import java.util.concurrent.*;

/**
 * 线程池Module
 * <p/>
 */
public class ExecutorServiceModule extends AbstractModule {
    private final static int defaultCoreSize = Runtime.getRuntime().availableProcessors();
    private final static int defaultMaxSize = defaultCoreSize * 5;
    private final static int defaultKeepAliveTimeMinutes = 1;
    private final static int defaultQueueSize = 5000;
    private final static RejectedExecutionHandler defaultRejectedExecutionHandler = new ThreadPoolExecutor.CallerRunsPolicy();

    private final String name;
    private final String executorConfPrefix;
    private final boolean needShutdown;

    /**
     * 使用默认配置,且需要shutdown的线程池
     *
     * @param name 线程池的名称 not null
     */
    public ExecutorServiceModule(String name) {
        this.name = Preconditions.checkNotNull(name);
        this.executorConfPrefix = null;
        this.needShutdown = false;
    }

    /**
     * @param name               线程池的名称
     * @param executorConfPrefix 线程池配置的前缀
     * @param needShutdown       是否需要shutdown
     */
    public ExecutorServiceModule(String name, String executorConfPrefix, boolean needShutdown) {
        this.name = Preconditions.checkNotNull(name);
        this.executorConfPrefix = Preconditions.checkNotNull(executorConfPrefix);
        this.needShutdown = needShutdown;
    }


    @Override
    protected void configure() {
        bind(ExecutorService.class).annotatedWith(Names.named(this.name)).toProvider(new ExectorServiceProvider());
    }

    /**
     * 调度服务Provider
     */
    public class ExectorServiceProvider implements Provider<ExecutorService> {
        @Inject
        private IConfigService conf;

        @Inject
        @ShutdownStage
        private IStageActionRegistry registry;

        public ExectorServiceProvider() {
        }


        @Override
        public ExecutorService get() {
            int coreSize = getIntConfig("coreSize", defaultCoreSize);
            int maxSize = getIntConfig("maxSize", defaultMaxSize);
            int keepAliveTime = getIntConfig("keepAliveTimeMinuts", defaultKeepAliveTimeMinutes);
            int queueSzie = getIntConfig("queueSize", defaultQueueSize);

            ThreadPoolExecutor executor = new ThreadPoolExecutor(coreSize, maxSize, keepAliveTime, TimeUnit.MINUTES, new LinkedBlockingQueue<>(queueSzie), new NamedThreadFactory(name));
            executor.setRejectedExecutionHandler(defaultRejectedExecutionHandler);
            if (needShutdown) {
                registry.addAction(() -> ThreadUtil.runQuitely(() -> ThreadUtil.shutdownAndAwaitTermination(executor, 60)));
            }
            return executor;
        }

        private int getIntConfig(String confName, int defaultValue) {
            if (executorConfPrefix != null) {
                return MapUtil.getInt(conf, executorConfPrefix + "." + confName, defaultValue);
            } else {
                return defaultValue;
            }
        }
    }
}

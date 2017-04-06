package com.babyfs.tk.service.biz.service.schedule.intergration.guice;

import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.babyfs.tk.commons.MapConfig;
import com.babyfs.tk.commons.config.IConfigService;
import com.babyfs.tk.commons.service.IStageActionRegistry;
import com.babyfs.tk.commons.service.LifecycleModule;
import com.babyfs.tk.commons.service.annotation.ShutdownStage;
import com.babyfs.tk.service.biz.service.schedule.IScheduleService;
import com.babyfs.tk.service.biz.service.schedule.internal.ScheduleServiceImpl;

/**
 * 调度器Module
 * <p/>
 */
public class ScheduleServiceModule extends AbstractModule {

    /**
     * 调度器线程池线程个数
     */
    private static final String CONF_SCHEDULE_POOL_SIZE = "schedule.pool.size";

    @Override
    protected void configure() {
        bind(IScheduleService.class).toProvider(new ScheduleServiceProvider()).asEagerSingleton();
        requestInjection(StageInit.class);
    }

    /**
     * 调度服务Provider
     */
    public static class ScheduleServiceProvider implements Provider<IScheduleService> {

        @Inject(optional = true)
        private IConfigService conf;

        @Override
        public IScheduleService get() {
            int poolSize = MapConfig.getInt(CONF_SCHEDULE_POOL_SIZE, conf, 1);
            return new ScheduleServiceImpl(poolSize);
        }
    }


    /**
     * 调度服务的初始化工作
     */
    private static final class StageInit {

        private StageInit() {

        }

        @Inject
        public static void setupShutdown(@ShutdownStage final IStageActionRegistry registry, final IScheduleService scheduleService) {
            Preconditions.checkArgument(registry != null, "The @ShutdownStage registry is null,please install the " + LifecycleModule.class.getName());
            registry.addAction(new ScheduleServiceShutDown(scheduleService));
        }

        /**
         * 调度服务关闭
         */
        private static class ScheduleServiceShutDown implements Runnable {

            /**
             * 调度服务
             */
            private final IScheduleService scheduleService;

            public ScheduleServiceShutDown(IScheduleService scheduleService) {
                this.scheduleService = scheduleService;
            }

            @Override
            public void run() {
                this.scheduleService.shutDown(true);
            }
        }

    }

}

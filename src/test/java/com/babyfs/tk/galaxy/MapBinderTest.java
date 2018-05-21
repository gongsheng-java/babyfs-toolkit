package com.babyfs.tk.galaxy;

import com.babyfs.tk.commons.application.LifeServiceBindUtil;
import com.babyfs.tk.commons.config.guice.ConfigServiceModule;
import com.babyfs.tk.commons.service.LifecycleModule;
import com.babyfs.tk.commons.service.ServiceModule;
import com.babyfs.tk.galaxy.demo.*;
import com.babyfs.tk.service.biz.schedule.guice.ExecutorServiceModule;
import com.google.inject.*;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Names;
import org.junit.Test;

/**
 * 测试基类,完成基本的模块注入
 */
public class MapBinderTest {

    @Inject
    private Health healthService;

    @Inject
    private BadService badService;

    @Test
    public void test() {
        ServicePoint health = new ServicePoint(Health.class, null);
        ServicePoint bad = new ServicePoint(BadService.class, null);
        ServicePoint mapTest = new ServicePoint(MapService.class, null);

        ExecutorServiceModule executorServiceModule = new ExecutorServiceModule("back");
        ConfigServiceModule configServiceModule = new ConfigServiceModule("globalconf.xml");

        final Key<BadService> badServiceKey = Key.get(BadService.class);
        final Key<Health> healthKey = Key.get(Health.class);
        final Key<IMapService> mapTestKey = Key.get(IMapService.class);
        Module module = new ServiceModule() {
            @Override
            protected void configure() {
                bindService(BadService.class, BadServiceImpl.class);
                bindService(Health.class, HealthImpl.class);
            }
        };

        Module m0 = new ServiceModule() {
            @Override
            protected void configure() {
                MapBinder<ServicePoint, Object> mapbinder = MapBinder.newMapBinder(binder(), ServicePoint.class, Object.class, Names.named("ok"));
                mapbinder.addBinding(bad).to(badServiceKey);
                //mapbinder.addBinding(mapTest).to(mapTestKey);
            }
        };

        Module m1 = new ServiceModule() {
            @Override
            protected void configure() {
                MapBinder<ServicePoint, Object> mapbinder = MapBinder.newMapBinder(binder(), ServicePoint.class, Object.class, Names.named("ok"));
                mapbinder.addBinding(bad).to(badServiceKey);
                //mapbinder.addBinding(mapTest).to(mapTestKey);
            }
        };

        Module webModule = new ServiceModule() {
            @Override
            protected void configure() {
                bindService(IMapService.class, MapService.class);
                LifeServiceBindUtil.addLifeService(binder(), IMapService.class);
                install(executorServiceModule);
                install(configServiceModule);
                install(new LifecycleModule());

                install(module);
                install(m0);
                install(m1);

            }
        };


        //Injector injector = Guice.createInjector(module, sync, new LifecycleModule(), executorServiceModule,configServiceModule);
        Injector injector = Guice.createInjector(webModule);
        injector.injectMembers(this);

    }

    public static class HealthProvider implements Provider<Health> {
        @Override
        public Health get() {
            System.out.println("create health");
            return new HealthImpl();
        }
    }
}

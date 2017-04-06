package com.babyfs.tk.dubbo.guice;

import com.google.inject.*;
import com.google.inject.name.Names;
import com.babyfs.tk.commons.service.IStageActionRegistry;
import com.babyfs.tk.commons.service.LifecycleModule;
import com.babyfs.tk.commons.service.annotation.InitStage;
import org.junit.Ignore;
import org.junit.Test;

public class DubboRPCModuleTest {
    @Test
    @Ignore
    public void testLoad() throws Exception {
        System.setProperty("java.net.preferIPv4Stack", "true");
        DubboRPCModule loader = new DubboRPCModule("dubbo-provider.xml");
        Module module = new AbstractModule() {
            @Override
            protected void configure() {
                bind(DemoService.class).annotatedWith(Names.named("demoRemote")).toInstance(new DemoImpl("remote"));
                bind(DemoService.class).annotatedWith(Names.named("demoLocal")).toInstance(new DemoImpl("local"));
            }
        };
        Injector injector = Guice.createInjector(loader, module, new LifecycleModule());
        IStageActionRegistry instance = injector.getInstance(Key.get(IStageActionRegistry.class, InitStage.class));
        instance.execute();

        Key<DemoService> remoteServiceKey = Key.get(DemoService.class, Names.named("remote"));
        Key<DemoService> localServiceKey = Key.get(DemoService.class, Names.named("local"));
        DemoService remoteDemoService = injector.getInstance(remoteServiceKey);
        DemoService localDemoService = injector.getInstance(localServiceKey);
        Thread.sleep(500);
        String msg = remoteDemoService.sayHello("teemo");
        System.out.println("msg:" + msg);
        msg = localDemoService.sayHello("teemo");
        System.out.println("msg:" + msg);
    }

    public static class DemoImpl implements DemoService {
        private final String from;

        public DemoImpl(String from) {
            this.from = from;
        }

        @Override
        public String sayHello(String name) {
            return name + " hello ,from" + from;
        }
    }
}
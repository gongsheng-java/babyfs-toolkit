package com.babyfs.tk.service.biz.schedule.guice;

import com.babyfs.tk.commons.config.guice.ConfigServiceModule;
import com.babyfs.tk.commons.service.LifecycleModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutorService;

/**
 *
 */
public class ExecutorServiceModuleTest {
    @Test
    public void configure() throws Exception {
        Injector injector = Guice.createInjector(new ExecutorServiceModule("test"), new LifecycleModule(), new ConfigServiceModule("globalconf.xml"));
        Use use0 = new Use();
        Use use1 = new Use();
        Use use2 = new Use();
        injector.injectMembers(use0);
        injector.injectMembers(use1);
        injector.injectMembers(use2);
        System.out.println(" " + use0.executorService + " " + use1.executorService + " " + use2.executorService);
        Assert.assertTrue(use0.executorService == use1.executorService);
        Assert.assertTrue(use1.executorService == use2.executorService);
    }

    public static class Use {
        @Inject
        @Named("test")
        public ExecutorService executorService;
    }
}
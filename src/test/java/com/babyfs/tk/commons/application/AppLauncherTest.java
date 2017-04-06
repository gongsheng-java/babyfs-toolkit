package com.babyfs.tk.commons.application;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.babyfs.tk.commons.service.LifeServiceSupport;
import org.junit.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 */
public class AppLauncherTest {
    private static String[] args = new String[]{TestApp.class.getName()};

    public static void main(String[] arg) {
        AppLauncher.main(args);
    }

    @Test
    public void testMain() throws Exception {
        AppLauncher.main(args);
    }

    public static class TestApp extends ApplicationSupport {
        private Runnable runnable = new Runnable() {
            @Override
            public void run() {
                System.out.println("I'm running");
            }
        };

        public TestApp() {
            Module demo = new Module() {
                @Override
                public void configure(Binder binder) {
                    LifeServiceBindUtil.addLifeService(binder, new LifeServiceSupport() {
                        private ScheduledExecutorService executorService;

                        @Override
                        protected void execStart() {
                            executorService = Executors.newScheduledThreadPool(1);
                            executorService.scheduleWithFixedDelay(runnable, 1, 500, TimeUnit.MILLISECONDS);
                        }

                        @Override
                        protected void execStop() {
                            executorService.shutdownNow();
                        }
                    });
                }
            };
            this.addModule(demo);
        }
    }
}

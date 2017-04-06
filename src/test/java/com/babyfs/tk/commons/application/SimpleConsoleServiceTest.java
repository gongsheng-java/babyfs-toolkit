package com.babyfs.tk.commons.application;

import com.google.common.base.Joiner;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.name.Named;
import org.junit.Test;

public class SimpleConsoleServiceTest {
    @Test
    public void main() {
        AppLauncher.main(new String[]{SimpleApp.class.getName(), "Hello","Guice"});
    }

    public static class EchoConsoleService extends SimpleConsoleService {
        @Inject
        @Named(IApplication.BIND_APP_ARGS_NAME)
        private String[] args;

        @Override
        public void main() {
            System.out.println(Joiner.on(",").join(args));
        }
    }

    public static class SimpleApp extends ApplicationSupport {
        @Override
        public void init(final String[] args) {
            super.init(args);
            super.addModule(new Module() {
                @Override
                public void configure(Binder binder) {
                    LifeServiceBindUtil.addLifeService(binder, EchoConsoleService.class);
                }
            });
        }
    }
}
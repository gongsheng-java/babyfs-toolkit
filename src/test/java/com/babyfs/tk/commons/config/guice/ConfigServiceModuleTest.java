package com.babyfs.tk.commons.config.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.util.Modules;
import com.babyfs.tk.commons.config.IConfigService;
import com.babyfs.tk.commons.service.LifecycleModule;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

public class ConfigServiceModuleTest {

    @Test
    public void test_schema() throws URISyntaxException {
        URI uri = new URI("classpath:///a.xml");
        print_scheme(uri);
        uri = new URI("zk:///node");
        print_scheme(uri);
        uri = new URI("sys:///empty");
        print_scheme(uri);
    }

    @Test
    public void test_configure() throws URISyntaxException {
        System.setProperty("who?","me");
        ConfigServiceModule module = new ConfigServiceModule("globalconf.xml");
        Injector injector = Guice.createInjector(module);
        IConfigService configService = injector.getInstance(IConfigService.class);
        Assert.assertNotNull(configService);
        Assert.assertEquals("王东永", configService.get("name1"));
        Assert.assertEquals("me", configService.get("who?"));
    }

    @Test
    @Ignore
    public void test_configure_withzk() throws URISyntaxException {
        System.setProperty("who?","me");
        ConfigServiceModule module = new ConfigServiceModule("globalconf_zk.xml");
        Injector injector = Guice.createInjector(module,new LifecycleModule());
        IConfigService configService = injector.getInstance(IConfigService.class);
        Assert.assertNotNull(configService);
        Assert.assertEquals("wangdongyong", configService.get("name1"));
        Assert.assertEquals("me", configService.get("who?"));
    }

    private void print_scheme(URI uri) {
        System.out.println("shceme:" + uri.getScheme()
                + ",host:" + uri.getHost()
                + ",path:" + uri.getPath()
                + ",query:" + uri.getQuery()
                + ",scheme_specific_part:" + uri.getSchemeSpecificPart());
    }

    @Test
    public void test_dup() {
        Injector injector = Guice.createInjector(Modules.override(new DemoModule(new DemoProvider2())).with(new DemoModule(new DemoProvider2())));
        Demo instance = injector.getInstance(Demo.class);
        System.out.println(instance);
    }

    public static interface Demo {
        void hello();
    }

    public static class DemoImpl implements Demo {
        @Override
        public void hello() {

        }
    }

    public static final class DemoModule extends AbstractModule {
        private final Provider<Demo> provider;

        public DemoModule() {
            this(null);
        }

        public DemoModule(Provider<Demo> provider) {
            this.provider = provider;
        }

        @Override
        protected void configure() {
            if (provider == null) {
                bind(Demo.class).toProvider(DemoProvider.class).asEagerSingleton();
            } else {
                bind(Demo.class).toProvider(provider).asEagerSingleton();
            }
        }
    }

    public static final class DemoProvider implements Provider<Demo> {
        @Override
        public Demo get() {
            System.out.println("ok");
            return new DemoImpl();
        }
    }

    public static final class DemoProvider2 implements Provider<Demo> {
        @Override
        public Demo get() {
            System.out.println("ok2");
            return new DemoImpl();
        }
    }
}
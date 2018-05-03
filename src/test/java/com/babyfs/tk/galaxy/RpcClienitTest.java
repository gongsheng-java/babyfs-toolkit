package com.babyfs.tk.galaxy;

import com.babyfs.tk.commons.config.ConfigServiceConfig;
import com.babyfs.tk.commons.config.IConfigService;
import com.babyfs.tk.commons.config.guice.ConfigServiceModule;
import com.babyfs.tk.commons.model.ServiceResponse;
import com.babyfs.tk.commons.service.ServiceModule;
import com.babyfs.tk.galaxy.codec.IDecoder;
import com.babyfs.tk.galaxy.codec.IEncoder;
import com.babyfs.tk.galaxy.demo.BadService;
import com.babyfs.tk.galaxy.demo.BadServiceImpl;
import com.babyfs.tk.galaxy.demo.Health;
import com.babyfs.tk.galaxy.demo.HealthImpl;
import com.babyfs.tk.galaxy.guice.*;
import com.babyfs.tk.galaxy.server.IServer;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Module;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

public class RpcClienitTest extends BaseTest {
    @Inject
    private IEncoder encoder;

    @Inject
    private IDecoder decoder;

    @BeforeClass
    public static void setUp() throws Exception {
        BaseTest.setUp(getSubModules());
    }


    @Test
    public void test() {
        Health health = injector.getInstance(Health.class);
        System.out.println(health);

        {
        }
    }

    protected static List<Module> getSubModules() {
        List<Module> modules = Lists.newArrayList();
        modules.add(new RpcSupportModule());
        modules.add(new RpcClientSupportModule());
        modules.add(new RpcClientServiceModule() {
            @Override
            protected void configure() {
                bindRPCService(Health.class);
                bindRPCService(BadService.class);
            }
        });
        modules.add(new ConfigServiceModule("globalconf.xml"));
        return modules;
    }
}

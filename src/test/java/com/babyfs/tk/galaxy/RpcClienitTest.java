package com.babyfs.tk.galaxy;

import com.babyfs.tk.commons.model.ServiceResponse;
import com.babyfs.tk.galaxy.codec.IDecoder;
import com.babyfs.tk.galaxy.codec.IEncoder;
import com.babyfs.tk.galaxy.demo.BadService;
import com.babyfs.tk.galaxy.demo.Health;
import com.babyfs.tk.galaxy.guice.*;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Module;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

@Ignore
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
    public void test() throws Exception {
        Health health = injector.getInstance(Health.class);
        System.out.println(health);
        for (int i = 0; i < 100; i++) {
            try {
                ServiceResponse<String> jsonTest = health.jsonTest(null);
                printResponseMsg(jsonTest);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(5000);
        }

        System.out.println("wait finish");

        System.in.read();
    }

    protected static List<Module> getSubModules() {
        List<Module> modules = Lists.newArrayList();
        modules.add(new RpcSupportModule());
        modules.add(new RpcClientSupportModule());
        modules.add(new RpcServerSupportModule());
        modules.add(new ZkServiceRegisterModule());
        modules.add(new ZkServiceNamesModule());
        modules.add(new RpcClientServiceModule() {
            @Override
            protected void configure() {
                bindRPCService(Health.class);
                bindRPCService(BadService.class);
            }
        });
        return modules;
    }
}

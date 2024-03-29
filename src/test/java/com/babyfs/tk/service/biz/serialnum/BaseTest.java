package com.babyfs.tk.service.biz.serialnum;

import com.babyfs.tk.commons.config.guice.ConfigServiceModule;
import com.babyfs.tk.commons.model.ServiceResponse;
import com.babyfs.tk.commons.service.IStageActionRegistry;
import com.babyfs.tk.commons.service.LifecycleModule;
import com.babyfs.tk.commons.service.annotation.InitStage;
import com.babyfs.tk.service.basic.es.ESClientFactory;
import com.babyfs.tk.service.biz.Modules;
import com.babyfs.tk.service.biz.counter.guice.IDSequenceServiceModule;
import com.babyfs.tk.service.biz.serialnum.guice.SerialNumServiceModule;
import com.google.common.collect.Lists;
import com.google.inject.*;
import org.junit.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 测试基类,完成基本的模块注入
 */
public abstract class BaseTest {
    protected static volatile Injector injector;
    private static final Object lock = new Object();
    private static IStageActionRegistry initActionRegistry;

    static {
        //禁用elasticsearch client检测
        System.setProperty(ESClientFactory.DISABLE_CHECK_CLIENT, "true");
    }

    /**
     * 加载Guice配置
     *
     * @throws Exception
     */
    @BeforeClass
    public static void setUp() {
        if (injector != null) {
            return;
        }

        synchronized (lock) {
            if (injector != null) {
                return;
            }
            Module module = getBaseModules();
            ArrayList<Module> modules = Lists.newArrayList(module);
            modules.add(new ConfigServiceModule("globalconf.xml"));
            injector = Guice.createInjector(modules);
            initActionRegistry.execute();
        }
    }

    @Inject
    public static void setupInit(@InitStage final IStageActionRegistry actionRegistry) {
        initActionRegistry = actionRegistry;
    }

    public static AbstractModule getBaseModules() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                install(new LifecycleModule());
                requestStaticInjection(BaseTest.class);
                install(Modules.BASIC_MODULE_REDIS_CONF);
                install(Modules.BASIC_MODULE_REDIS_SERVICE);
                install(new IDSequenceServiceModule());
                install(new SerialNumServiceModule());
            }
        };
    }

    @AfterClass
    public static void shutdown() {

    }

    @Before
    public void setUpTest() {
        injector.injectMembers(this);
    }

    @Test
    @Ignore
    public void test() throws InterruptedException, Exception {
        System.out.println("test guice init.");
    }

    protected void printResponseMsg(ServiceResponse response) {
        if (response != null && !response.isSuccess()) {
            System.out.println("code:" + response.getCode() + ",fail,response msg:" + response.getMsg());
        }
    }
}

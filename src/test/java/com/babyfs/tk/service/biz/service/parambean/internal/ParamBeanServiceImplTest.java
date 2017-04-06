package com.babyfs.tk.service.biz.service.parambean.internal;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.name.Names;
import com.babyfs.tk.commons.GlobalKeys;
import com.babyfs.tk.service.biz.factory.StaticModuleFactory;
import com.babyfs.tk.service.biz.service.parambean.DataBindException;
import com.babyfs.tk.service.biz.service.parambean.IParamBeanService;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * ParamBeanServiceImpl测试类
 * <p/>
 */
public class ParamBeanServiceImplTest {
    private static IParamBeanService paramBeanService;

    @BeforeClass
    public static void setUp() throws Exception {
        List<Module> modules = new ArrayList<Module>();
        modules.add(StaticModuleFactory.BASE_MODULE_VALIDATE_SERVICE);
        modules.add(StaticModuleFactory.BASE_MODULE_PARAM_BEAN_SERVICE);
        modules.add(new AbstractModule() {
            @Override
            protected void configure() {
                bindConstant().annotatedWith(Names.named(GlobalKeys.VALIDATION_RULE_CONF)).to("validation_rule.xml");
                bind(Class[].class).annotatedWith(Names.named(GlobalKeys.PARAM_BEAN_CLASSES)).toInstance(new Class[]{
                        ITestBean.class, TestBean.class
                });
            }
        });
        Injector injector = Guice.createInjector(modules);
        paramBeanService = injector.getInstance(IParamBeanService.class);
        assertNotNull(paramBeanService);
    }

    @Test
    public void testBuildParamBean() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("i", "1");
        request.addParameter("s", "notempty");

        ITestBean bean = paramBeanService.buildParamBean(request, ITestBean.class);

        assertEquals(new Integer(1), bean.getInteger());
        assertEquals("notempty", bean.getString());
        assertEquals("jiahao.fang@renren-inc.com", bean.getOptionalEmail());
        assertEquals(new Integer(2), bean.getOptionalInt());

        request.addParameter("os", "test@test.com");
        request.addParameter("oi", "10");

        bean = paramBeanService.buildParamBean(request, ITestBean.class);

        assertEquals("test@test.com", bean.getOptionalEmail());
        assertEquals(new Integer(10), bean.getOptionalInt());

        request.setParameter("i", "a");

        try {
            bean = paramBeanService.buildParamBean(request, ITestBean.class);
            fail("should throw an exception");
        } catch (DataBindException e) {
            assertEquals("i", e.getFieldName());
        }

        // 测试 Integer为空的情况
        MockHttpServletRequest request2 = new MockHttpServletRequest();
        request2.addParameter("i", (String) null);

        ITestBean bean2 = paramBeanService.buildParamBeanNoValidation(request2, ITestBean.class);
        assertNull(bean2.getInteger());
    }

    @Test
    public void testBuildParamBean2() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("i", "1");
        request.addParameter("s", "notempty");

        TestBean bean = paramBeanService.buildParamBean(request, TestBean.class);

        assertEquals(new Integer(1), bean.getInteger());
        assertEquals("notempty", bean.getString());


        request.addParameter("my", "my");
        bean = paramBeanService.buildParamBean(request, TestBean.class);
        assertNull(bean.getMy());
        bean.setMy("my");
        assertEquals("my", bean.getMy());

        request.setParameter("i", "a");

        try {
            bean = paramBeanService.buildParamBean(request, TestBean.class);
            fail("should throw an exception");
        } catch (DataBindException e) {
            assertEquals("i", e.getFieldName());
        }

        // 测试 Integer为空的情况
        MockHttpServletRequest request2 = new MockHttpServletRequest();
        request2.addParameter("i", (String) null);

        TestBean bean2 = paramBeanService.buildParamBeanNoValidation(request2, TestBean.class);
        assertNull(bean2.getInteger());
    }
}

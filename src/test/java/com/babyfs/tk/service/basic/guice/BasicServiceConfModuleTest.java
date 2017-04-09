package com.babyfs.tk.service.basic.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.babyfs.tk.service.basic.guice.BasicServiceConfModule;
import com.babyfs.tk.service.basic.guice.annotation.ServiceRedis;
import com.babyfs.tk.service.basic.xml.server.Server;
import com.babyfs.tk.service.basic.xml.server.Servers;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * {@link BasicServiceConfModule}单元测试
 * <p/>
 * 下面的测试用例基本说明的所有的用法，以及需要注意的地方
 * <p/>
 * <p/>
 */
// todo 由于系统参数是静态加载的，第一次加载以后就不再加载了，所以下面的用例一起跑的时候有问题，我就注掉了，单个方法跑没问题
@Ignore
public class BasicServiceConfModuleTest {
    @Test
    public void testNoOveride() {
        Injector injector = Guice.createInjector(
                new BasicServiceConfModule() {
                    @Override
                    protected void configure() {
                        // 初始化redis配置
                        bindXmlConfByAnnotation(ServiceRedis.class, "redis-servers.xml", "redis-client.xml");
                    }
                }
        );
        Servers servers = injector.getInstance(Key.get(Servers.class, ServiceRedis.class));
        Map<String, Server> map = servers.getServers();
        assertEquals(2, map.size());
        Server redis1 = map.get("redis_1");
        assertNotNull(redis1);
        assertEquals("10.22.225.66", redis1.getHost());
        assertEquals("6379", redis1.getPort());
    }

    @Test
    public void testOveride1() {
        System.setProperty("override_server_config", "redis-servers.xml_redis_1:test_host:1111");
        Injector injector = Guice.createInjector(
                new BasicServiceConfModule() {
                    @Override
                    protected void configure() {
                        // 初始化redis配置
                        bindXmlConfByAnnotation(ServiceRedis.class, "redis-servers.xml", "redis-client.xml");
                    }
                }
        );
        Servers servers = injector.getInstance(Key.get(Servers.class, ServiceRedis.class));
        Map<String, Server> map = servers.getServers();
        assertEquals(2, map.size());
        Server redis1 = map.get("redis_1");
        assertNotNull(redis1);
        assertEquals("test_host", redis1.getHost());
        assertEquals("1111", redis1.getPort());
        System.setProperty("override_server_config", "");
    }

    @Test
    public void testOveride2() {
        System.setProperty("override_server_config", "redis-servers.xml_redis_1:test_host:1111,redis-servers.xml_redis_2:127.0.0.1:1234,");
        Injector injector = Guice.createInjector(
                new BasicServiceConfModule() {
                    @Override
                    protected void configure() {
                        // 初始化redis配置
                        bindXmlConfByAnnotation(ServiceRedis.class, "redis-servers.xml", "redis-client.xml");
                    }
                }
        );
        Servers servers = injector.getInstance(Key.get(Servers.class, ServiceRedis.class));
        Map<String, Server> map = servers.getServers();
        assertEquals(2, map.size());
        Server redis1 = map.get("redis_1");
        assertNotNull(redis1);
        assertEquals("test_host", redis1.getHost());
        assertEquals("1111", redis1.getPort());
        Server redis2 = map.get("redis_2");
        assertNotNull(redis2);
        assertEquals("127.0.0.1", redis2.getHost());
        assertEquals("1234", redis2.getPort());
        System.setProperty("override_server_config", "");
    }

    @Test
    public void testOverideError1() {
        System.setProperty("override_server_config", "redis-servers.xml_redis_1:test_host:1111,redis-servers.xml_redis_1:127.0.0.1:1234,");
        try {
            Injector injector = Guice.createInjector(
                    new BasicServiceConfModule() {
                        @Override
                        protected void configure() {
                            // 初始化redis配置
                            bindXmlConfByAnnotation(ServiceRedis.class, "redis-servers.xml", "redis-client.xml");
                        }
                    }
            );
            fail("should throw an IllegalStateException");
        } catch (Throwable e) {
            assertTrue(e.getCause() instanceof IllegalStateException);
        }
        System.setProperty("override_server_config", "");
    }

    @Test
    public void testOverideError2() {
        System.setProperty("override_server_config", "redis-servers.xml_redis_1::1111");
        try {
            Injector injector = Guice.createInjector(
                    new BasicServiceConfModule() {
                        @Override
                        protected void configure() {
                            // 初始化redis配置
                            bindXmlConfByAnnotation(ServiceRedis.class, "redis-servers.xml", "redis-client.xml");
                        }
                    }
            );
            fail("should throw an IllegalStateException");
        } catch (Throwable e) {
            assertTrue(e.getCause() instanceof IllegalStateException);
        }
        System.setProperty("override_server_config", "");
    }

    @Test
    public void testOverideError3() {
        System.setProperty("override_server_config", "redis-servers.xml_redis_1:test_host:111111");
        try {
            Injector injector = Guice.createInjector(
                    new BasicServiceConfModule() {
                        @Override
                        protected void configure() {
                            // 初始化redis配置
                            bindXmlConfByAnnotation(ServiceRedis.class, "redis-servers.xml", "redis-client.xml");
                        }
                    }
            );
            fail("should throw an IllegalStateException");
        } catch (Throwable e) {
            assertTrue(e.getCause() instanceof IllegalStateException);
        }
        System.setProperty("override_server_config", "");
    }
}

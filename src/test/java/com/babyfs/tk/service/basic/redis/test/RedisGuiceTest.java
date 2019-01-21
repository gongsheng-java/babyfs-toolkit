package com.babyfs.tk.service.basic.redis.test;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.babyfs.tk.commons.MapConfig;
import com.babyfs.tk.commons.config.IConfigService;
import com.babyfs.tk.commons.config.internal.ConfigServiceMapImpl;
import com.babyfs.tk.service.basic.INameResourceService;
import com.babyfs.tk.service.basic.guice.BasicServiceConfModule;
import com.babyfs.tk.service.basic.guice.BasicServiceModule;
import com.babyfs.tk.service.basic.guice.BasicServiceModuleProviders;
import com.babyfs.tk.service.basic.guice.annotation.ServiceRedis;
import com.babyfs.tk.service.basic.redis.IRedis;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;

/**
 * 测试基于guice集成的redis
 * <p/>
 */
public class RedisGuiceTest {

    @Inject
    @ServiceRedis
    INameResourceService<IRedis> cacheService;

    @Inject
    @ServiceRedis
    INameResourceService<JedisPool> redisPoolService;

    @Before
    public void setUp() throws Exception {
        Module confgModule = new AbstractModule() {
            @Override
            protected void configure() {
                // 配置模块
               //  bind(IConfigService.class).toInstance(new ConfigServiceMapImpl(MapConfig.pasreConf("redis.conf.xml")));
            }

        };

        BasicServiceConfModule redisConfgModule = new BasicServiceConfModule() {
            @Override
            protected void configure() {
                // 初始化redis配置
                bindXmlConfByAnnotation(ServiceRedis.class, "redis-servers.xml", "redis-client.xml");
            }
        };

        BasicServiceModule redisServiceModule = new BasicServiceModule() {
            @Override
            protected void configure() {
                // 初始化redis配置
                bindBasicService(ServiceRedis.class, IRedis.class, BasicServiceModuleProviders.ShardedRedisServiceProvider.class);
               // bindBasicService(ServiceRedis.class, JedisPoolAbstract.class, BasicServiceModuleProviders.JedisPoolServiceProvider.class);
            }
        };



        ArrayList<Module> modules = Lists.newArrayList(confgModule, redisConfgModule, redisServiceModule);
        Injector injector = Guice.createInjector(modules);
        injector.injectMembers(this);

    }

    @Test
    public void testSet() {

        try {

            IRedis client = (IRedis) this.cacheService.get("user.list");

            client.del("set_test_1");
            client.del("set_test_2");

            // test sadd
            long res = client.sadd("set_test_1", "test");
            Assert.assertEquals(1, res);

            // test smembers
            Set<String> setStrRes = client.smembers("set_test_1");
            Assert.assertTrue(setStrRes != null);
            Assert.assertTrue(setStrRes.size() == 1);
            Assert.assertTrue(setStrRes.contains("test"));

            // test sismember
            boolean bolRes = client.sismember("set_test_1", "test");
            Assert.assertTrue(bolRes);

            // test scard
            res = client.scard("set_test_1");
            Assert.assertTrue(res == 1);

            TestModelApple testObj = new TestModelApple();

            // test saddObject
            res = client.saddObject("set_test_2", testObj);
            Assert.assertEquals(1, res);

            // test smembersObject
            Set<TestObj> setObjRes = client.smembersObject("set_test_2");
            Assert.assertTrue(setObjRes != null);
            Assert.assertTrue(setObjRes.size() == 1);

            // test sismemberObject
            bolRes = client.sismemberObject("set_test_2", testObj);
            Assert.assertTrue(bolRes);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testSentinel(){
//        try {
//            JedisPoolAbstract pool =  this.redisPoolService.get("subscribe");
//            Jedis jedis = pool.getResource();
//            String timespan = String.valueOf(System.currentTimeMillis()) ;
//            String key = "testkey";
//            jedis.set(key,timespan);
//            jedis.expire(key,200);
//            String result = jedis.get(key);
//            Assert.assertEquals(result,timespan);
//        }
//        catch (Exception ex){
//            ex.printStackTrace();
//        }
    }


    /**
     * 测试对象
     */
    private class TestObj implements Serializable {

        private static final long serialVersionUID = -7105469292102582078L;

        private String testStr;

        public TestObj(String testStr) {
            this.testStr = testStr;
        }

        public String getTestStr() {
            return testStr;
        }

        public void setTestStr(String testStr) {
            this.testStr = testStr;
        }
    }



}

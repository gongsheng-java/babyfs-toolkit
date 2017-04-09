package com.babyfs.tk.service.biz.counter;

import com.babyfs.tk.commons.base.Pair;
import com.babyfs.tk.commons.xml.JAXBUtil;
import com.babyfs.tk.service.basic.CommonNameResourceService;
import com.babyfs.tk.service.basic.INameResourceService;
import com.babyfs.tk.service.basic.redis.IRedis;
import com.babyfs.tk.service.basic.redis.client.RedisConfig;
import com.babyfs.tk.service.basic.redis.client.ShardedRedisServiceLoaderImpl;
import com.babyfs.tk.service.basic.xml.client.ServiceGroup;
import com.babyfs.tk.service.basic.xml.server.Servers;
import com.babyfs.tk.service.biz.cache.CacheParameter;
import com.babyfs.tk.service.biz.counter.impl.RedisCounterService;
import com.babyfs.tk.service.biz.counter.impl.RedisCounterSyncService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class RedisCounterServiceTest {
    @Test
    @Ignore
    public void test_All() throws Exception {
        Servers servers = JAXBUtil.unmarshal(Servers.class, "redis-servers.xml");
        ServiceGroup serverGroup = JAXBUtil.unmarshal(ServiceGroup.class, "redis-client.xml");

        Map<String, Long> initMap = Maps.newHashMap();
        initMap.put("test", 11L);
        ICounterPersistService counterPersistService = Mockito.mock(ICounterPersistService.class);
        Mockito.when(counterPersistService.get(1, "1")).thenReturn(initMap);
        Mockito.when(counterPersistService.del(1, "1")).thenReturn(true);

        Mockito.when(counterPersistService.get(2, "1")).thenReturn(initMap);
        Mockito.when(counterPersistService.del(2, "1")).thenReturn(true);

        Mockito.when(counterPersistService.sync(Mockito.eq(1), Mockito.eq("1"), Mockito.anyMapOf(String.class, Long.class))).thenReturn(true);
        Mockito.when(counterPersistService.sync(Mockito.eq(2), Mockito.eq("1"), Mockito.anyMapOf(String.class, Long.class))).thenReturn(true);
        CacheParameter cacheParameter = new CacheParameter(0, "counter", "cc_", "");

        INameResourceService<IRedis> nameResourceService = new CommonNameResourceService<>(new ShardedRedisServiceLoaderImpl(new RedisConfig(), servers, serverGroup));
        RedisCounterService redisCounterService = new RedisCounterService("test", cacheParameter, counterPersistService, 5, nameResourceService);
        RedisCounterSyncService syncService = new RedisCounterSyncService(nameResourceService, cacheParameter, redisCounterService, 2, 3, 3, 5);

        //测试更新和读取
        redisCounterService.del(1, "1");
        for (int i = 1; i <= 100; i++) {
            Assert.assertTrue(redisCounterService.incr(1, "1", Lists.newArrayList(Pair.of("test", 1L))));
            Map<String, Long> counterMap = redisCounterService.get(1, "1");
            Assert.assertEquals(i + 11, counterMap.get("test").longValue());
        }

        Assert.assertTrue(redisCounterService.syncCounter(1, "1"));

        syncService.scanAll();
        // 测试按时间淘汰
        Thread.sleep(5000);
        syncService.scanAll();

        redisCounterService = new RedisCounterService("test", cacheParameter, counterPersistService, 1, nameResourceService);
        syncService = new RedisCounterSyncService(nameResourceService, cacheParameter, redisCounterService, 1, 3, 3, 5);

        redisCounterService.del(1, "1");
        redisCounterService.del(2, "1");
        Assert.assertTrue(redisCounterService.incr(1, "1", Lists.newArrayList(Pair.of("test", 1L))));
        Assert.assertTrue(redisCounterService.incr(2, "1", Lists.newArrayList(Pair.of("test", 1L))));
        redisCounterService.del(1, "1");
        redisCounterService.del(2, "1");
        Assert.assertTrue(redisCounterService.incr(1, "1", Lists.newArrayList(Pair.of("test", 1L))));
        Assert.assertTrue(redisCounterService.incr(2, "1", Lists.newArrayList(Pair.of("test", 1L))));
        Map<String, Long> counterMap = redisCounterService.get(1, "1");
        Assert.assertEquals(1 + 11, counterMap.get("test").longValue());

        // 测试按个数淘汰
        syncService.scanAll();

        // 测试过期
        Thread.sleep(5000);
        syncService.scanAll();
    }

    @Test
    @Ignore
    public void bench() throws Exception {
        Servers servers = JAXBUtil.unmarshal(Servers.class, "redis-servers.xml");
        ServiceGroup serverGroup = JAXBUtil.unmarshal(ServiceGroup.class, "redis-client.xml");

        Map<String, Long> initMap = Maps.newHashMap();
        initMap.put("test", 11L);
        ICounterPersistService counterPersistService = Mockito.mock(ICounterPersistService.class);
        Mockito.when(counterPersistService.get(1, "1")).thenReturn(initMap);
        Mockito.when(counterPersistService.del(1, "1")).thenReturn(true);

        INameResourceService<IRedis> nameResourceService = new CommonNameResourceService<>(new ShardedRedisServiceLoaderImpl(new RedisConfig(), servers, serverGroup));
        CacheParameter cacheParameter = new CacheParameter(0, "counter", "cc_", "");

        RedisCounterService redisCounterService = new RedisCounterService("test", cacheParameter, counterPersistService, 1, nameResourceService);
        redisCounterService.del(1, "1");
        for (int i = 1; i <= 100; i++) {
            Assert.assertTrue(redisCounterService.incr(1, "1", Lists.newArrayList(Pair.of("test", 1L))));
            Map<String, Long> counterMap = redisCounterService.get(1, "1");
            Assert.assertEquals(i + 11, counterMap.get("test").longValue());
        }

        System.gc();

        redisCounterService.del(1, "1");
        long st = System.nanoTime();
        ArrayList<Pair<String, Long>> test = Lists.newArrayList(Pair.of("test", 1L));
        for (int i = 1; i <= 50000; i++) {
            redisCounterService.incr(1, "1", test);
            //Map<String, Long> counterMap = redisCounterService.get(1, "1");
        }
        long et = System.nanoTime();
        System.out.println("int time:" + (et - st) / TimeUnit.MILLISECONDS.toNanos(1));

        System.gc();
        st = System.nanoTime();
        for (int i = 1; i <= 50000; i++) {
            Map<String, Long> counterMap = redisCounterService.get(1, "1");
            if (counterMap.isEmpty()) {
                throw new IllegalArgumentException();
            }
        }
        et = System.nanoTime();
        System.out.println("get time:" + (et - st) / TimeUnit.MILLISECONDS.toNanos(1));

        redisCounterService.del(1, "1");
        Map<String, Long> map = redisCounterService.get(1, "1");
        Assert.assertEquals(11L, map.get("test").longValue());
    }
}
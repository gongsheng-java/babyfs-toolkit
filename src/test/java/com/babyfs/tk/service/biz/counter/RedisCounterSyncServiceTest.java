package com.babyfs.tk.service.biz.counter;

import com.babyfs.tk.commons.base.Pair;
import com.babyfs.tk.commons.xml.JAXBUtil;
import com.babyfs.tk.service.basic.CommonNameResourceService;
import com.babyfs.tk.service.basic.INameResourceService;
import com.babyfs.tk.service.basic.redis.IRedis;
import com.babyfs.tk.service.basic.redis.client.PipelineFunc;
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
import redis.clients.jedis.ShardedJedisPipeline;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class RedisCounterSyncServiceTest {
    @Test
    @Ignore
    public void testScanAll() throws Exception {
        Servers servers = JAXBUtil.unmarshal(Servers.class, "redis-servers.xml");
        ServiceGroup serverGroup = JAXBUtil.unmarshal(ServiceGroup.class, "redis-client.xml");

        Map<String, Long> initMap = Maps.newHashMap();
        initMap.put("test", 11L);
        ICounterPersistService counterPersistService = Mockito.mock(ICounterPersistService.class);
        Mockito.when(counterPersistService.get(Mockito.anyInt(), Mockito.anyString())).thenReturn(initMap);
        Mockito.when(counterPersistService.del(Mockito.anyInt(), Mockito.anyString())).thenReturn(true);
        Mockito.when(counterPersistService.sync(Mockito.anyInt(), Mockito.anyString(), Mockito.anyMapOf(String.class, Long.class))).thenReturn(true);

        CacheParameter cacheParameter = new CacheParameter(0, "counter", "counter_", "");

        final int slots = 5;
        INameResourceService<IRedis> nameResourceService = new CommonNameResourceService<>(new ShardedRedisServiceLoaderImpl(new RedisConfig(), servers, serverGroup));
        RedisCounterService redisCounterService = new RedisCounterService("test", cacheParameter, counterPersistService, slots, nameResourceService);
        RedisCounterSyncService syncService = new RedisCounterSyncService(nameResourceService, cacheParameter, redisCounterService, 2, 3, 3, 5);

        ArrayList<Pair<String, Long>> delta = Lists.newArrayList(Pair.of("test", 1L));
        //测试更新和读取
        for (int i = 1; i <= 10000; i++) {
            String id = String.valueOf(i);
            Assert.assertTrue(redisCounterService.incr(1, id, delta));
            Map<String, Long> counterMap = redisCounterService.get(1, id);
            Assert.assertEquals(1 + 11, counterMap.get("test").longValue());
        }

        long st = System.currentTimeMillis();
        syncService.scanAll();
        System.out.println("scanAll:" + (System.currentTimeMillis() - st) + "ms");

        //每个slot这是应该只剩下两个计数器了
        for (int i = 0; i < slots; i++) {
            String syncSetSlotKey = redisCounterService.getSyncSetSlotKey(i);
            List<Object> pipelined = syncService.getSyncSetRedis().pipelined(new PipelineFunc("") {
                @Nullable
                @Override
                public Void apply(@Nullable ShardedJedisPipeline input) {
                    input.zcard(syncSetSlotKey);
                    input.zrange(syncSetSlotKey, 0, -1);
                    return null;
                }
            });
            long left = ((Long) pipelined.get(0)).longValue();
            Set<String> counterKeys = (Set<String>) pipelined.get(1);
            Assert.assertEquals(2, left);
            Assert.assertEquals(left, counterKeys.size());
            for (String counterKey : counterKeys) {
                System.out.println(counterKey);
                Pair<Integer, String> pair = CounterConst.parseCounterTypeAndId(counterKey);
                redisCounterService.del(pair.first, pair.second);
            }
        }

        //500个,30秒同步间隔,60秒过期间隔
        syncService = new RedisCounterSyncService(nameResourceService, cacheParameter, redisCounterService, 500, 3, 30, 60);

        //测试更新和读取
        for (int i = 1; i <= 10000; i++) {
            String id = String.valueOf(i);
            Assert.assertTrue(redisCounterService.incr(1, id, delta));
        }

        st = System.currentTimeMillis();
        syncService.scanAll();
        System.out.println("scanAll:" + (System.currentTimeMillis() - st) + "ms");

        //每个slot这是应该只剩下两个500个计数器
        for (int i = 0; i < slots; i++) {
            String syncSetSlotKey = redisCounterService.getSyncSetSlotKey(i);
            List<Object> pipelined = syncService.getSyncSetRedis().pipelined(new PipelineFunc("") {
                @Nullable
                @Override
                public Void apply(@Nullable ShardedJedisPipeline input) {
                    input.zcard(syncSetSlotKey);
                    input.zrange(syncSetSlotKey, 0, -1);
                    return null;
                }
            });
            long left = ((Long) pipelined.get(0)).longValue();
            Set<String> counterKeys = (Set<String>) pipelined.get(1);
            Assert.assertEquals(500, left);
            Assert.assertEquals(left, counterKeys.size());
            for (String counterKey : counterKeys) {
                Pair<Integer, String> pair = CounterConst.parseCounterTypeAndId(counterKey);
                redisCounterService.del(pair.first, pair.second);
            }
        }
    }

}
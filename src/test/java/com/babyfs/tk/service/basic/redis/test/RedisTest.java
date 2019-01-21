package com.babyfs.tk.service.basic.redis.test;

import com.babyfs.tk.commons.codec.util.HessianCodecUtil;
import com.babyfs.tk.commons.xml.JAXBUtil;
import com.babyfs.tk.service.basic.CommonNameResourceService;
import com.babyfs.tk.service.basic.redis.IRedis;
import com.babyfs.tk.service.basic.redis.client.JRedisPoolServiceLoaderImpl;
import com.babyfs.tk.service.basic.redis.client.RedisConfig;
import com.babyfs.tk.service.basic.redis.client.ShardedRedisServiceLoaderImpl;
import com.babyfs.tk.service.basic.xml.client.ServiceGroup;
import com.babyfs.tk.service.basic.xml.server.Servers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import redis.clients.jedis.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 */
public class RedisTest {

    public static void putLong(byte[] bb, long x, int index) {
        bb[index + 0] = (byte) (x >> 56);
        bb[index + 1] = (byte) (x >> 48);
        bb[index + 2] = (byte) (x >> 40);
        bb[index + 3] = (byte) (x >> 32);
        bb[index + 4] = (byte) (x >> 24);
        bb[index + 5] = (byte) (x >> 16);
        bb[index + 6] = (byte) (x >> 8);
        bb[index + 7] = (byte) (x >> 0);
    }

    public static byte[] long2Byte(long x) {
        byte[] bb = new byte[8];
        bb[0] = (byte) (x >> 56);
        bb[1] = (byte) (x >> 48);
        bb[2] = (byte) (x >> 40);
        bb[3] = (byte) (x >> 32);
        bb[4] = (byte) (x >> 24);
        bb[5] = (byte) (x >> 16);
        bb[6] = (byte) (x >> 8);
        bb[7] = (byte) (x >> 0);
        return bb;
    }

    public static long bytes2Long(byte[] bb) {
        return ((((long) bb[0] & 0xff) << 56)
                | (((long) bb[1] & 0xff) << 48)
                | (((long) bb[2] & 0xff) << 40)
                | (((long) bb[3] & 0xff) << 32)
                | (((long) bb[4] & 0xff) << 24)
                | (((long) bb[5] & 0xff) << 16)
                | (((long) bb[6] & 0xff) << 8)
                | (((long) bb[7] & 0xff) << 0));
    }

    public static long getLong(byte[] bb, int index) {
        return ((((long) bb[index + 0] & 0xff) << 56)
                | (((long) bb[index + 1] & 0xff) << 48)
                | (((long) bb[index + 2] & 0xff) << 40)
                | (((long) bb[index + 3] & 0xff) << 32)
                | (((long) bb[index + 4] & 0xff) << 24)
                | (((long) bb[index + 5] & 0xff) << 16)
                | (((long) bb[index + 6] & 0xff) << 8) |
                (((long) bb[index + 7] & 0xff) << 0));
    }

    final byte[] bfoo = {0x04, 0x02, 0x03, 0x01, 0x04, 0x05, 0x06, 0x07};
    final byte[] bfoo1 = {0x00, 0x00, 0x00, 0x00, 0x04, 0x05, 0x06, 0x07};

    private static int count = 0;
    private static long startId = 60001999l;
    private static String KEY_DEF = "mymsg_75892321";
    private static ShardedJedis staticJedis;

    @Before
    public void setUp() {
        try {
            List<JedisShardInfo> shards = new ArrayList<JedisShardInfo>();
            shards.add(new JedisShardInfo("localhost"));
            shards.add(new JedisShardInfo("localhost"));
            staticJedis = new ShardedJedis(shards);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testPipeline() throws Exception {

        final String key = "test_pipeline_key";
        final String value = "test_pipeline_value";
        final byte[] byteKey = key.getBytes("utf-8");
        final byte[] byteValue = HessianCodecUtil.encode(value);

        try {
            staticJedis.set(byteKey, byteValue);

            ShardedJedisPipeline pipelined = staticJedis.pipelined();
            pipelined.get(byteKey);
            pipelined.get(byteKey);

            List<Object> results = pipelined.syncAndReturnAll();
            Assert.assertTrue(results.size() == 2);
            String values1 = String.valueOf(HessianCodecUtil.decode((byte[]) results.get(0)));
            String values2 = String.valueOf(HessianCodecUtil.decode((byte[]) results.get(1)));
            Assert.assertTrue(values1.equals(values2));

            Long rest = staticJedis.zrank("test2", "1000");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testDataServicePipe() throws Exception {

        try {
            // 先塞2000条数据
            long id = 60000000l;
            String key = "mymsg_75892321";
            for (; id <= 60001999l; id++) {
                staticJedis.zadd(key, 1, String.valueOf(id));
            }

            // 再塞10000条数据，且需要trim, 保持2000条
            long start = System.nanoTime();
            for (; count <= 9999; count++) {

                ShardedJedisPipeline pipelined = staticJedis.pipelined();
                pipelined.zadd(KEY_DEF, 1, String.valueOf(startId));
                pipelined.zremrangeByRank(KEY_DEF, 0, 0);
                pipelined.sync();
                startId++;
            }
            long end = System.nanoTime();
            System.out.println("增加 10000 个数据花费时间 : " + (end - start) / 1000000 + " 毫秒");

            long count = staticJedis.zcard(key);
            System.out.println("当前数据量：" + count);
            Set<String> datas = (Set<String>) staticJedis.zrange(key, 0, (int) count);
            Object[] datasArr = datas.toArray();
            System.out.println("data type:" + datas.getClass() + ",当前最大数据：" + datasArr[datasArr.length - 1]);
            System.out.println("当前最小数据：" + datasArr[0]);


            // 测试按范围获取  每页100个，取10000次
            start = end;
            Random random = new Random();
            for (int i = 0; i <= 9999; i++) {
                int ran = random.nextInt(1800);
                datas = staticJedis.zrange(key, ran, ran + 100);
                datasArr = datas.toArray();
            }
            end = System.nanoTime();
            System.out.println("查询10000次，每次100个数据花费时间 : " + (end - start) / 1000000 + " 毫秒");
            // 删除测试数据
            staticJedis.zremrangeByRank(key, 0, 999999999);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDataService() throws Exception {
        try {

            // 先塞2000条数据
            Jedis jedis = new Jedis("localhost");
            long id = 60000000l;
            String key = "mymsg_75892321";
            for (; id <= 60001999l; id++) {
                jedis.zadd(key, 1, String.valueOf(id));
            }

            // 再塞10000条数据，且需要trim, 保持2000条
            long start = System.nanoTime();
            for (int i = 0; i <= 9999; i++) {
                jedis.zadd(key, 1, String.valueOf(id));
                jedis.zremrangeByRank(key, 0, 0);
                id++;
            }
            long end = System.nanoTime();
            System.out.println("增加 10000 个数据花费时间 : " + (end - start) / 1000000 + " 毫秒");

            long count = jedis.zcard(key);
            System.out.println("当前数据量：" + count);
            Set<String> datas = jedis.zrange(key, 0, (int) count);
            Object[] datasArr = datas.toArray();
            System.out.println("当前最大数据：" + datasArr[datasArr.length - 1]);
            System.out.println("当前最小数据：" + datasArr[0]);


            // 测试按范围获取  每页100个，取10000次
            start = end;
            Random random = new Random();
            for (int i = 0; i <= 9999; i++) {
                int ran = random.nextInt(1800);
                datas = jedis.zrange(key, ran, ran + 100);
            }
            end = System.nanoTime();
            System.out.println("查询10000次，每次100个数据花费时间 : " + (end - start) / 1000000 + " 毫秒");

            // 删除测试数据
            jedis.zremrangeByRank(key, 0, 999999999);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testRanSortSetPushPipe() throws Exception {
        try {
            List<JedisShardInfo> shards = new ArrayList<JedisShardInfo>();
            shards.add(new JedisShardInfo("localhost"));
            shards.add(new JedisShardInfo("localhost"));
            ShardedJedis jedis = new ShardedJedis(shards);


            long start = System.nanoTime();

            ShardedJedisPipeline pipelined = jedis.pipelined();

            Random random = new Random();

            String key = "msgs_29812341";
            long idData = 288796629002420000l;
            for (int i = 0; i <= 9999; i++) {
                pipelined.zadd(key, 1, String.valueOf(idData + random.nextInt(100000)));
            }
            pipelined.sync();
            long end = System.nanoTime();
            long use = end - start;
            long msUse = use / 1000000;
            System.out.println("增加 10000 个数据花费时间 : " + use + " 纳秒， " + msUse + " 毫秒, 合计每秒 : ");

            // 删除数据
            jedis.zremrangeByRank("msgs_29812341", 0, 999999999);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testSortSetPushPipe() throws Exception {
        try {
            List<JedisShardInfo> shards = new ArrayList<JedisShardInfo>();
            shards.add(new JedisShardInfo("localhost"));
            shards.add(new JedisShardInfo("localhost"));
            ShardedJedis jedis = new ShardedJedis(shards);


            long start = System.nanoTime();

            ShardedJedisPipeline pipelined = jedis.pipelined();
            String key = "msgs_29812341";
            long idData = 288796629002420000l;
            for (; idData <= 288796629002429999l; idData++) {
                pipelined.zadd(key, idData, String.valueOf(idData));
            }
            pipelined.sync();

            long end = System.nanoTime();

            long use = end - start;
            long msUse = use / 1000000;
            System.out.println("增加 10000 个数据花费时间 : " + use + " 纳秒， " + msUse + " 毫秒, 合计每秒 : ");

            start = end;
            ShardedJedisPipeline p2 = jedis.pipelined();
            key = "msgs_29812341";
            idData = 288796629002420000l;
            for (; idData <= 288796629002429999l; idData++) {
                p2.zrem(key, String.valueOf(idData));
            }
            p2.sync();

            end = System.nanoTime();

            use = end - start;
            msUse = use / 1000000;
            System.out.println("删除 10000 个数据花费时间 : " + use + " 纳秒， " + msUse + " 毫秒, 合计每秒 : ");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSortSetPushMulti() throws Exception {
        try {

            Jedis jedis = new Jedis("localhost");

            Transaction trans = jedis.multi();
            String key = "msgs_29812341";
            long idData = 288796629002420000l;
            int count = 0;
            long start = System.nanoTime();

            for (; idData <= 288796629002429999l; idData++) {
                trans.zadd(key, 1, String.valueOf(idData));
                //jedis.zadd(keyByte, 1, long2Byte(idData));
                count++;
            }
            List<Object> response = trans.exec();

            long end = System.nanoTime();

            long use = end - start;
            long msUse = use / 1000000;
            System.out.println("增加 " + count + " 个数据花费时间 : " + use + " 纳秒， " + msUse + " 毫秒, 合计每秒 : ");


            trans = jedis.multi();
            start = end;
            idData = 288796629002420000l;
            count = 0;
            for (; idData <= 288796629002429999l; idData++) {
                trans.zrem(key, String.valueOf(idData));
                //jedis.zrem(keyByte, long2Byte(idData));
                count++;
            }

            response = trans.exec();

            end = System.nanoTime();

            use = end - start;
            msUse = use / 1000000;
            System.out.println("删除 " + count + " 个数据花费时间 : " + use + " 纳秒， " + msUse + " 毫秒, 合计每秒 : ");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testRanSortSetPushOne() throws Exception {
        try {
            Jedis jedis = new Jedis("localhost");
            String key = "msgs_29812341";
            long idData = 288796629002420000l;

            Random random = new Random();

            int count = 0;

            long start = System.nanoTime();
            for (int i = 0; i <= 9999; i++) {
                long id = idData + random.nextInt(100000);
                jedis.zadd(key, (double) id, String.valueOf(id));
                count++;
            }

            long end = System.nanoTime();

            long use = end - start;
            long msUse = use / 1000000;
            System.out.println("增加 " + count + " 个数据花费时间 : " + use + " 纳秒， " + msUse + " 毫秒, 合计每秒 : ");

            // 删除数据
            jedis.zremrangeByRank(key, 0, 999999999);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSortSetPushOne() throws Exception {
        try {
            Jedis jedis = new Jedis("localhost");
            String key = "msgs_29812341";
            byte[] keyByte = key.getBytes();
            long idData = 288796629002420000l;

            int count = 0;

            long start = System.nanoTime();

            for (; idData <= 288796629002429999l; idData++) {
                jedis.zadd(key, 1, String.valueOf(idData));
                //jedis.zadd(keyByte, 1, long2Byte(idData));
                count++;
            }

            long end = System.nanoTime();

            long use = end - start;
            long msUse = use / 1000000;
            System.out.println("增加 " + count + " 个数据花费时间 : " + use + " 纳秒， " + msUse + " 毫秒, 合计每秒 : ");

            start = end;
            idData = 288796629002420000l;
            count = 0;
            for (; idData <= 288796629002429999l; idData++) {
                jedis.zrem(key, String.valueOf(idData));
                //jedis.zrem(keyByte, long2Byte(idData));
                count++;
            }
            end = System.nanoTime();

            use = end - start;
            msUse = use / 1000000;
            System.out.println("删除 " + count + " 个数据花费时间 : " + use + " 纳秒， " + msUse + " 毫秒, 合计每秒 : ");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    @Ignore
    public void testGet() throws Exception {
        Servers redisServers = JAXBUtil.unmarshal(Servers.class, "redis-servers.xml");
        ServiceGroup serviceGroup = JAXBUtil.unmarshal(ServiceGroup.class, "redis-client.xml");
        CommonNameResourceService<IRedis> redisService = new CommonNameResourceService<IRedis>(new ShardedRedisServiceLoaderImpl(new RedisConfig(), redisServers, serviceGroup));
        IRedis redisClient = redisService.get("user.list");
        Assert.assertNotNull(redisClient);
        for (int i = 0; i < 10; i++) {
            redisClient.set("hi" + i, "hi" + i, 0);
        }
        for (int i = 0; i < 10; i++) {
            String value = redisClient.get("hi" + i);
            Assert.assertEquals("hi" + i, "hi" + i);
        }
    }

    @Test
    @Ignore
    public void test() throws Exception {
        Servers redisServers = JAXBUtil.unmarshal(Servers.class, "redis-servers.xml");
        ServiceGroup serviceGroup = JAXBUtil.unmarshal(ServiceGroup.class, "redis-client.xml");
        CommonNameResourceService<IRedis> redisService = new CommonNameResourceService<IRedis>(new ShardedRedisServiceLoaderImpl(new RedisConfig(), redisServers, serviceGroup));
        IRedis redisClient = redisService.get("user.list");
        Assert.assertNotNull(redisClient);
        redisClient.setObject("applebin", new TestModelApple(), 0);
        TestModelApple apple = (TestModelApple) redisClient.getObject("applebin", 0);
        System.out.println("[apple]" + apple.getStr() + "_" + apple.getF() + "_" + apple.getIn() + "_" + apple.getL());
    }

    @Test
    @Ignore
    public void testJedisPool() throws Exception {
//        Servers redisServers = JAXBUtil.unmarshal(Servers.class, "redis-servers.xml");
//        ServiceGroup serviceGroup = JAXBUtil.unmarshal(ServiceGroup.class, "redis-client.xml");
//        //CommonNameResourceService<JedisPool> redisService = new CommonNameResourceService<JedisPool>(new JRedisPoolServiceLoaderImpl(new RedisConfig(), redisServers, serviceGroup));
//        CommonNameResourceService<JedisPoolAbstract> redisService = new CommonNameResourceService<JedisPoolAbstract>(new JRedisPoolServiceLoaderImpl(new RedisConfig(), redisServers, serviceGroup));
//
//        JedisPoolAbstract jedisPool = redisService.get("subscribe");
//        Assert.assertNotNull(jedisPool);
    }
}

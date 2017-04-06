package com.babyfs.tk.servcie.basic.redis.client;

import com.babyfs.tk.service.basic.redis.client.PipelineFunction;
import com.babyfs.tk.service.basic.redis.client.RedisImpl;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.hash.Hashing;
import com.babyfs.tk.commons.Constants;
import com.babyfs.tk.commons.base.Pair;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import redis.clients.jedis.*;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 */
public class RedisImplTest {

    @Test
    @Ignore
    public void testGetObject() throws Exception {
        ShardedJedisPool pool = getShardedJedisPool();
        RedisImpl redis = new RedisImpl(pool);
        for (int i = 0; i < 100; i++) {
            Object o = redis.getObject("d0ngw.test0000", 10);
            System.out.println("o:" + i + " " + o);
        }
        ArrayList<String> objects = Lists.newArrayList();
        Set<String> sets = Sets.newHashSet();
        Map<? extends Serializable, ? extends Serializable> maps = Maps.newHashMap();

        redis.setObject("a", objects, 0);
        redis.setObject("b", sets, 0);
        redis.setObject("c", maps, 0);
        List<String> ra = redis.getObject("a", 0);
        System.out.println(ra.getClass());
        pool.destroy();
    }

    @Test
    @Ignore
    public void test_eval() throws InterruptedException {
        ShardedJedisPool pool = getShardedJedisPool();
        final RedisImpl redis = new RedisImpl(pool);
        //测试简单的脚本
        String simpleScript = "return KEYS[1] ";
        String scriptSHA1 = Hashing.sha1().hashString(simpleScript, Constants.DEFAULT_CHARSET_OBJ).toString();
        test_script(redis, "eval_t", simpleScript, scriptSHA1);

        //测试带有过期时间的incr
        final String incrWithExcpireScript = "local current\n" +
                "current = redis.call(\"incr\",KEYS[1])\n" +
                "if tonumber(current) == 1 then\n" +
                "    redis.call(\"expire\",KEYS[1],ARGV[1])\n" +
                "end\n" +
                "return current";
        final String incrWithExcpireScriptSHA1 = Hashing.sha1().hashString(incrWithExcpireScript, Constants.DEFAULT_CHARSET_OBJ).toString();
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (int i = 0; i < 10; i++) {
            final String key = "incr_test" + i;
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    test_script(redis, key, incrWithExcpireScript, incrWithExcpireScriptSHA1, "60");
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(1L, TimeUnit.DAYS);
        for (int i = 0; i < 10; i++) {
            final String key = "incr_test" + i;
            String s = redis.get(key);
            System.out.println(s);
        }
    }

    @Test
    @Ignore
    public void test_template() {
        ShardedJedisPool shardedJedisPool = getShardedJedisPool();
        final RedisImpl redis = new RedisImpl(shardedJedisPool);
        Pair<String, Long> templateVal = redis.template(new Function<ShardedJedis, Pair<String, Long>>() {
            @Nullable
            @Override
            public Pair<String, Long> apply(@Nullable ShardedJedis input) {
                input.set("template", "temp");
                String val = input.get("template");
                Assert.assertEquals(val, "temp");
                Long temp = input.incr("temp");
                Assert.assertEquals(1L, temp.longValue());
                input.del("template");
                input.del("temp");
                return Pair.of(val, temp);
            }
        });
        Assert.assertNotNull(templateVal);
        Assert.assertEquals("temp", templateVal.first);
        Assert.assertEquals(1L, templateVal.getSecond().longValue());
    }

    @Test
    @Ignore
    public void test_pipeline() {
        ShardedJedisPool shardedJedisPool = getShardedJedisPool();
        RedisImpl redis = new RedisImpl(shardedJedisPool);
        List<Object> results = redis.pipelined(new PipelineFunction("test") {
            @Nullable
            @Override
            public Void apply(@Nullable ShardedJedisPipeline input) {
                input.del("a");
                input.del("b");
                input.del("c");
                input.set("a", "b");
                input.incr("b");
                input.set("c", "c");
                input.get("a");
                input.get("b");
                input.get("c");
                input.del("a");
                input.del("b");
                input.del("c");
                input.get("a");
                input.get("b");
                input.get("c");
                return null;
            }
        });

        Assert.assertNotNull(results);
        System.out.println("results size:" + results.size());
        for (Object v : results) {
            System.out.println(v);
        }
    }

    private void test_script(RedisImpl redis, String key, String script, String scriptSHA1, String... args) {
        for (int i = 0; i < 10; i++) {
            long st = System.nanoTime();
            Object eval = redis.eval(key, script, scriptSHA1, args);
            long et = System.nanoTime();
            //Assert.assertEquals(i+1,eval.intValue());
            System.out.println((et - st) / 1000 / 1000 + "ms");
            System.out.println("result type:" + eval.getClass() + " value:" + eval);
        }
    }

    private ShardedJedisPool getShardedJedisPool() {
        JedisPoolConfig config = new JedisPoolConfig();
        List<JedisShardInfo> shards = new ArrayList<JedisShardInfo>();
        JedisShardInfo info = new JedisShardInfo("127.0.0.1", 6379);
        shards.add(info);
        return new ShardedJedisPool(config, shards);
    }

}

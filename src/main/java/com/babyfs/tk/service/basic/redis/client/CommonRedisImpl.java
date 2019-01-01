package com.babyfs.tk.service.basic.redis.client;

import com.babyfs.tk.commons.Constants;
import com.babyfs.tk.commons.codec.ICodec;
import com.babyfs.tk.commons.codec.impl.HessianCodec;
import com.babyfs.tk.service.basic.redis.IRedis;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import io.prometheus.client.Summary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisException;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.Future;

/**
 * Redis服务的封装 哨兵模式，只支持单个Redis
 * <p/>
 */
public class CommonRedisImpl implements IRedis {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommonRedisImpl.class);
    private static final ICodec DEFAULT_CODEC = new HessianCodec();

    static final Summary redisCallLatency = Summary.build()
            .name("redis_call_latency_seconds")
            .labelNames("method", "success")
            .quantile(0.98, 0.005)
            .quantile(0.85, 0.005)
            .quantile(0.50, 0.005)
            .help("Request latency in seconds.").register();

    /**
     * 编码和解码器
     */
    private final ICodec codec;

    /**
     * 基于shard的jedis客户端池
     */
    private final JedisPoolAbstract pool;

    /**
     * 是否启用Probe
     */
    private final boolean enableProbe;

    /**
     * 默认使用{@link HessianCodec}作为编码器
     *
     * @param pool
     */
    public CommonRedisImpl(JedisPoolAbstract pool) {
        this(pool, DEFAULT_CODEC, false);
    }

    /**
     * @param pool
     * @param codec
     */
    public CommonRedisImpl(JedisPoolAbstract pool, ICodec codec, boolean enableProbe) {
        Preconditions.checkArgument(pool != null);
        Preconditions.checkArgument(codec != null);
        this.pool = pool;
        this.codec = codec;
        this.enableProbe = enableProbe;
    }


    /**
     * 获取Set的成员数量
     *
     * @param key key
     * @return
     */
    @Override
    public Long scard(String key) {
        Jedis jedis = pool.getResource();
        boolean success = true;
        final long st = System.nanoTime();
        try {
            return jedis.scard(key);
        } catch (Exception e) {
            success = false;
            throw new JedisException(jedis.toString(), e);
        } finally {
            close(jedis);
            metric("scard", st, success);
        }
    }

    /**
     * 往Set里面增加一个对象成员
     *
     * @param key
     * @param value
     * @param <T>
     * @return
     */
    @Override
    public <T extends Serializable> Long saddObject(String key, T value) {
        Jedis jedis = pool.getResource();
        // 性能监控数据初始化
        final long st = System.nanoTime();
        boolean success = true;
        try {
            final byte[] keyBytes = getStringBytes(key);
            final byte[] valueBytes = codec.encode(value);
            return jedis.sadd(keyBytes, valueBytes);
        } catch (Exception e) {
            success = false;
            throw new JedisException(jedis.toString(), e);
        } finally {
            close(jedis);
            metric("saddObject", st, success);
        }
    }

    /**
     * 查询Set中某成员是否存在
     *
     * @param key
     * @param member
     * @return
     */
    @Override
    public boolean sismember(final String key, final String member) {
        Jedis jedis = pool.getResource();
        // 性能监控数据初始化
        final long st = System.nanoTime();
        boolean success = true;
        try {
            return jedis.sismember(key, member);
        } catch (Exception e) {
            success = false;
            throw new JedisException(jedis.toString(), e);
        } finally {
            close(jedis);
            metric("sismember", st, success);
        }
    }

    /**
     * 查询Set中某成员是否存在 (Serializable成员 ：Object类型)
     *
     * @param key
     * @param value
     * @param <T>
     * @return
     */
    @Override
    public <T extends Serializable> Boolean sismemberObject(String key, T value) {
        Jedis jedis = pool.getResource();
        // 性能监控数据初始化
        final long st = System.nanoTime();
        boolean success = true;
        try {
            final byte[] keyBytes = getStringBytes(key);
            final byte[] valueBytes = codec.encode(value);
            return jedis.sismember(keyBytes, valueBytes);
        } catch (Exception e) {
            success = false;
            throw new JedisException(jedis.toString(), e);
        } finally {
            close(jedis);
            metric("sismemberObject", st, success);
        }
    }

    /**
     * 获取所有Set成员（String类型成员）
     *
     * @param key
     * @return
     */
    @Override
    public Set<String> smembers(final String key) {
        Jedis jedis = pool.getResource();
        // 性能监控数据初始化
        final long st = System.nanoTime();
        boolean success = true;
        try {
            return jedis.smembers(key);
        } catch (Exception e) {
            success = false;
            throw new JedisException(jedis.toString(), e);
        } finally {
            close(jedis);
            metric("smembers", st, success);
        }
    }

    /**
     * 获取所有Set成员(Serializable成员 ：Object类型)
     *
     * @param key
     * @param <T>
     * @return
     */
    @Override
    public <T extends Serializable> Set<T> smembersObject(String key) {
        Jedis jedis = pool.getResource();
        // 性能监控数据初始化
        final long st = System.nanoTime();
        boolean success = true;
        try {
            final byte[] keyBytes = getStringBytes(key);
            Set<byte[]> set = jedis.smembers(keyBytes);
            Set<T> result = new HashSet<T>();
            for (byte[] b : set) {
                T t = (T) codec.decode(b);
                result.add(t);
            }
            return result;
        } catch (Exception e) {
            success = false;
            throw new JedisException(jedis.toString(), e);
        } finally {
            close(jedis);
            metric("smembersObject", st, success);
        }

    }

    @Override
    public Long hincr(final String key, final String field, final long value) {
        Jedis jedis = pool.getResource();
        // 性能监控数据初始化
        final long st = System.nanoTime();
        boolean success = true;
        try {
            return jedis.hincrBy(key, field, value);
        } catch (Exception e) {
            success = false;
            throw new JedisException(jedis.toString(), e);
        } finally {
            close(jedis);
            metric("hincr", st, success);
        }
    }

    @Override
    public void hincr(final String key, final String field, final long value, final int expireSeconds) {
        Jedis jedis = pool.getResource();
        // 性能监控数据初始化
        final long st = System.nanoTime();
        boolean success = true;
        try {
            if (expireSeconds > 0) {
                Pipeline pipelined = jedis.pipelined();
                pipelined.hincrBy(key, field, (int) value);
                pipelined.expire(key, expireSeconds);
                pipelined.sync();
            } else {
                //不设置过期时间
                jedis.hincrBy(key, field, value);
            }
        } catch (Exception e) {
            success = false;
            throw new JedisException(jedis.toString(), e);
        } finally {
            close(jedis);
            metric("hincrexpire", st, success);
        }
    }

    @Override
    public String hget(String key, String field) {
        Jedis jedis = pool.getResource();
        // 性能监控数据初始化
        final long st = System.nanoTime();
        boolean success = true;
        try {
            return jedis.hget(key, field);
        } catch (Exception e) {
            success = false;
            throw new JedisException(jedis.toString(), e);
        } finally {
            close(jedis);
            metric("hget", st, success);
        }
    }

    @Override
    public byte[] hget(byte[] key, byte[] field) {
        Jedis jedis = pool.getResource();
        // 性能监控数据初始化
        final long st = System.nanoTime();
        boolean success = true;
        try {
            return jedis.hget(key, field);
        } catch (Exception e) {
            success = false;
            throw new JedisException(jedis.toString(), e);
        } finally {
            close(jedis);
            metric("hgetByte", st, success);
        }
    }

    @Override
    public boolean hexists(String key, String field) {
        Jedis jedis = pool.getResource();
        final long st = System.nanoTime();
        boolean success = true;
        try {
            return jedis.hexists(key, field);
        } catch (Exception e) {
            success = false;
            throw new JedisException(jedis.toString(), e);
        } finally {
            close(jedis);
            metric("hexists", st, success);
        }
    }

    @Override
    public Map<String, String> hgetAll(String key) {
        Jedis jedis = pool.getResource();
        final long st = System.nanoTime();
        boolean success = true;
        try {
            return jedis.hgetAll(key);
        } catch (Exception e) {
            success = false;
            throw new JedisException(jedis.toString(), e);
        } finally {
            close(jedis);
            metric("hgetAll", st, success);
        }
    }

    @Override
    public List<String> hmget(String key, String... fields) {
        Jedis jedis = pool.getResource();
        final long st = System.nanoTime();
        boolean success = true;
        try {
            return jedis.hmget(key, fields);
        } catch (Exception e) {
            success = false;
            throw new JedisException(jedis.toString(), e);
        } finally {
            close(jedis);
            metric("hmget", st, success);
        }
    }

    @Override
    public Set<String> hkeys(String key) {
        Jedis jedis = pool.getResource();
        // 性能监控数据初始化
        final long st = System.nanoTime();
        boolean success = true;
        try {
            return jedis.hkeys(key);
        } catch (Exception e) {
            success = false;
            throw new JedisException(jedis.toString(), e);
        } finally {
            close(jedis);
            metric("hkeys", st, success);
        }
    }

    @Override
    public List<String> hvals(String key) {
        Jedis jedis = pool.getResource();
        final long st = System.nanoTime();
        boolean success = true;
        try {
            return jedis.hvals(key);
        } catch (Exception e) {
            success = false;
            throw new JedisException(jedis.toString(), e);
        } finally {
            close(jedis);
            metric("hvals", st, success);
        }
    }

    @Override
    public Long hset(String key, String field, String value) {
        Jedis jedis = pool.getResource();
        // 性能监控数据初始化
        final long st = System.nanoTime();
        boolean success = true;
        try {
            return jedis.hset(key, field, value);
        } catch (Exception e) {
            success = false;
            throw new JedisException(jedis.toString(), e);
        } finally {
            close(jedis);
            metric("hset", st, success);
        }
    }

    @Override
    public Long hset(byte[] key, byte[] field, byte[] value) {
        Jedis jedis = pool.getResource();
        // 性能监控数据初始化
        final long st = System.nanoTime();
        boolean success = true;
        try {
            return jedis.hset(key, field, value);
        } catch (Exception e) {
            success = false;
            throw new JedisException(jedis.toString(), e);
        } finally {
            close(jedis);
            metric("hsetByte", st, success);
        }
    }

    @Override
    public Long hlen(String key) {
        Jedis jedis = pool.getResource();
        // 性能监控数据初始化
        final long st = System.nanoTime();
        boolean success = true;
        try {
            return jedis.hlen(key);
        } catch (Exception e) {
            success = false;
            throw new JedisException(jedis.toString(), e);
        } finally {
            close(jedis);
            metric("hlen", st, success);
        }
    }

    @Override
    public Long hdel(String key, String field) {
        Jedis jedis = pool.getResource();
        // 性能监控数据初始化
        final long st = System.nanoTime();
        boolean success = true;
        try {
            return jedis.hdel(key, field);
        } catch (Exception e) {
            success = false;
            throw new JedisException(jedis.toString(), e);
        } finally {
            close(jedis);
            metric("hdel", st, success);
        }
    }

    @Override
    public Long hdel(byte[] key, byte[] field) {
        Jedis jedis = pool.getResource();
        // 性能监控数据初始化
        final long st = System.nanoTime();
        boolean success = true;
        try {
            return jedis.hdel(key, field);
        } catch (Exception e) {
            success = false;
            throw new JedisException(jedis.toString(), e);
        } finally {
            close(jedis);
            metric("hdelByte", st, success);
        }
    }

    @Override
    public Long del(String key) {
        Jedis jedis = pool.getResource();
        // 性能监控数据初始化
        final long st = System.nanoTime();
        boolean success = true;
        try {
            return jedis.del(key);
        } catch (Exception e) {
            success = false;
            throw new JedisException(jedis.toString(), e);
        } finally {
            close(jedis);
            metric("del", st, success);
        }
    }

    @Override
    public Long rpush(String key, String string) {
        Jedis jedis = pool.getResource();
        // 性能监控数据初始化
        final long st = System.nanoTime();
        boolean success = true;
        try {
            return jedis.rpush(key, string);
        } catch (Exception e) {
            success = false;
            throw new JedisException(jedis.toString(), e);
        } finally {
            close(jedis);
            metric("rpush", st, success);
        }
    }

    @Override
    public Long lpush(String key, String string) {
        Jedis jedis = pool.getResource();
        // 性能监控数据初始化
        final long st = System.nanoTime();
        boolean success = true;
        try {
            return jedis.lpush(key, string);
        } catch (Exception e) {
            success = false;
            throw new JedisException(jedis.toString(), e);
        } finally {
            close(jedis);
            metric("lpush", st, success);
        }
    }

    @Override
    public Long llen(String key) {
        Jedis jedis = pool.getResource();
        // 性能监控数据初始化
        final long st = System.nanoTime();
        boolean success = true;
        try {
            return jedis.llen(key);

        } catch (Exception e) {
            success = false;
            throw new JedisException(jedis.toString(), e);
        } finally {
            close(jedis);
            metric("llen", st, success);
        }
    }

    @Override
    public List<String> lrange(String key, long start, long end) {
        Jedis jedis = pool.getResource();
        // 性能监控数据初始化
        final long st = System.nanoTime();
        boolean success = true;
        try {
            return jedis.lrange(key, start, end);

        } catch (Exception e) {
            success = false;
            throw new JedisException(jedis.toString(), e);
        } finally {
            close(jedis);
            metric("lrange", st, success);
        }
    }

    @Override
    public String ltrim(String key, long start, long end) {
        Jedis jedis = pool.getResource();
        // 性能监控数据初始化
        final long st = System.nanoTime();
        boolean success = true;
        try {
            return jedis.ltrim(key, start, end);
        } catch (Exception e) {
            success = false;
            throw new JedisException(jedis.toString(), e);
        } finally {
            close(jedis);
            metric("ltrim", st, success);
        }
    }

    @Override
    public String lindex(String key, long index) {
        Jedis jedis = pool.getResource();
        // 性能监控数据初始化
        final long st = System.nanoTime();
        boolean success = true;
        try {
            return jedis.lindex(key, index);
        } catch (Exception e) {
            success = false;
            throw new JedisException(jedis.toString(), e);
        } finally {
            close(jedis);
            metric("lindex", st, success);
        }
    }

    @Override
    public String lset(String key, long index, String value) {
        Jedis jedis = pool.getResource();
        // 性能监控数据初始化
        final long st = System.nanoTime();
        boolean success = true;
        try {
            return jedis.lset(key, index, value);
        } catch (Exception e) {
            success = false;
            throw new JedisException(jedis.toString(), e);
        } finally {
            close(jedis);
            metric("lset", st, success);
        }
    }

    @Override
    public Long lrem(String key, long count, String value) {
        Jedis jedis = pool.getResource();
        // 性能监控数据初始化
        final long st = System.nanoTime();
        boolean success = true;
        try {
            return jedis.lrem(key, count, value);
        } catch (Exception e) {
            success = false;
            throw new JedisException(jedis.toString(), e);
        } finally {
            close(jedis);
            metric("lrem", st, success);
        }
    }

    @Override
    public String lpop(String key) {
        Jedis jedis = pool.getResource();
        // 性能监控数据初始化
        final long st = System.nanoTime();
        boolean success = true;
        try {
            return jedis.lpop(key);
        } catch (Exception e) {
            success = false;
            throw new JedisException(jedis.toString(), e);
        } finally {
            close(jedis);
            metric("lpop", st, success);
        }
    }

    @Override
    public String rpop(String key) {
        Jedis jedis = pool.getResource();
        // 性能监控数据初始化
        final long st = System.nanoTime();
        boolean success = true;
        try {
            return jedis.rpop(key);
        } catch (Exception e) {
            success = false;
            throw new JedisException(jedis.toString(), e);
        } finally {
            close(jedis);
            metric("rpop", st, success);
        }
    }


    @Override
    public Long incr(String key) {
        Jedis jedis = pool.getResource();
        // 性能监控数据初始化
        final long st = System.nanoTime();
        boolean success = true;
        try {
            return jedis.incr(key);
        } catch (Exception e) {
            success = false;
            throw new JedisException(jedis.toString(), e);
        } finally {
            close(jedis);
            metric("incr", st, success);
        }
    }

    @Override
    public Boolean exists(String key) {
        Jedis jedis = pool.getResource();
        // 性能监控数据初始化
        final long st = System.nanoTime();
        boolean success = true;
        try {
            return jedis.exists(key);
        } catch (Exception e) {
            success = false;
            throw new JedisException(jedis.toString(), e);
        } finally {
            close(jedis);
            metric("exists", st, success);
        }
    }

    @Override
    public Long incr(final String key, final int expireSec) {
        Jedis jedis = pool.getResource();
        // 性能监控数据初始化
        final long st = System.nanoTime();
        boolean success = true;
        try {
            Long value;
            if (expireSec > 0) {
                Pipeline pipelined = jedis.pipelined();
                pipelined.incr(key);
                pipelined.expire(key, expireSec);
                List<Object> results = pipelined.syncAndReturnAll();
                value = (Long) results.get(0);
            } else {
                //不设置过期时间
                value = jedis.incr(key);
            }
            return value;
        } catch (Exception e) {
            success = false;
            throw new JedisException(jedis.toString(), e);
        } finally {
            close(jedis);
            metric("incrExpire", st, success);
        }
    }


    @Override
    public Long expire(String key, int seconds) {
        Jedis jedis = pool.getResource();
        final long st = System.nanoTime();
        boolean success = true;
        try {
            return jedis.expire(key, seconds);
        } catch (Exception e) {
            success = false;
            throw new JedisException(jedis.toString(), e);
        } finally {
            close(jedis);
            metric("expire", st, success);
        }
    }

    /**
     * SortSet操作 ：获取数据成员的索引，按照反排序（最大的成员索引最小）
     *
     * @param key
     * @param member
     * @return
     */
    @Override
    public Long zrevrank(String key, String member) {
        Jedis jedis = pool.getResource();
        // 性能监控数据初始化
        final long st = System.nanoTime();
        boolean success = true;
        try {
            return jedis.zrevrank(key, member);
        } catch (Exception e) {
            success = false;
            throw new JedisException(jedis.toString(), e);
        } finally {
            close(jedis);
            metric("zrevrank", st, success);
        }
    }

    /**
     * SortSet操作 ：获取set中成员总数
     *
     * @param key
     * @return
     */
    @Override
    public Long zcard(String key) {
        Jedis jedis = pool.getResource();
        // 性能监控数据初始化
        final long st = System.nanoTime();
        boolean success = true;
        try {
            return jedis.zcard(key);
        } catch (Exception e) {
            success = false;
            throw new JedisException(jedis.toString(), e);
        } finally {
            close(jedis);
            metric("zcard", st, success);
        }
    }

    /**
     * 根据byte[] key 获得 byte[] 类型数据
     *
     * @param key
     * @return
     */
    @Override
    public byte[] get(byte[] key) {
        Jedis jedis = pool.getResource();
        // 性能监控数据初始化
        final long st = System.nanoTime();
        boolean success = true;
        try {
            return jedis.get(key);
        } catch (Exception e) {
            success = false;
            throw new JedisException(jedis.toString(), e);
        } finally {
            close(jedis);
            metric("getByte", st, success);
        }
    }

    /**
     * 根据byte[] key 存入 byte[] 类型数据
     *
     * @param key
     * @param value
     * @return
     */
    @Override
    public String set(byte[] key, byte[] value) {
        Jedis jedis = pool.getResource();
        // 性能监控数据初始化
        final long st = System.nanoTime();
        boolean success = true;
        try {
            return jedis.set(key, value);
        } catch (Exception e) {
            success = false;
            throw new JedisException(jedis.toString(), e);
        } finally {
            close(jedis);
            metric("setByte", st, success);
        }
    }

    /**
     * 设置基于 byte[] key 的失效时间
     *
     * @param key
     * @param seconds
     * @return
     */
    @Override
    public Long expire(byte[] key, int seconds) {
        Jedis jedis = pool.getResource();
        // 性能监控数据初始化
        final long st = System.nanoTime();
        boolean success = true;
        try {
            return jedis.expire(key, seconds);
        } catch (Exception e) {
            success = false;
            throw new JedisException(jedis.toString(), e);
        } finally {
            close(jedis);
            metric("expireByte", st, success);
        }
    }

    /**
     * SortSet ： 删除一个成员
     *
     * @param key
     * @param member
     * @return
     */
    @Override
    public Long zrem(String key, String member) {
        Jedis jedis = pool.getResource();
        // 性能监控数据初始化
        final long st = System.nanoTime();
        boolean success = true;
        try {
            return jedis.zrem(key, member);
        } catch (Exception e) {
            success = false;
            throw new JedisException(jedis.toString(), e);
        } finally {
            close(jedis);
            metric("zrem", st, success);
        }
    }

    /**
     * 将指定key的值减1
     *
     * @param key
     * @return
     */
    @Override
    public Long decr(String key) {
        Jedis jedis = pool.getResource();
        // 性能监控数据初始化
        final long st = System.nanoTime();
        boolean success = true;
        try {
            return jedis.decr(key);
        } catch (Exception e) {
            success = false;
            throw new JedisException(jedis.toString(), e);
        } finally {
            close(jedis);
            metric("decr", st, success);
        }
    }

    @Override
    public Long decr(final String key, final int expireSec) {
        Jedis jedis = pool.getResource();
        // 性能监控数据初始化
        final long st = System.nanoTime();
        boolean success = true;
        try {
            Long value;
            if (expireSec > 0) {
                Pipeline pipelined = jedis.pipelined();
                pipelined.decr(key);
                pipelined.expire(key, expireSec);
                List<Object> results = pipelined.syncAndReturnAll();
                value = (Long) results.get(0);
            } else {
                //不设置过期时间
                value = jedis.decr(key);
            }
            return value;
        } catch (Exception e) {
            success = false;
            throw new JedisException(jedis.toString(), e);
        } finally {
            close(jedis);
            metric("decrExpire", st, success);
        }
    }

    @Override
    public Long ttl(String key) {
        Jedis jedis = pool.getResource();
        final long st = System.nanoTime();
        boolean success = true;
        try {
            return jedis.ttl(key);
        } catch (Exception e) {
            success = false;
            throw new JedisException(jedis.toString(), e);
        } finally {
            close(jedis);
            metric("ttl", st, success);
        }
    }

    @Override
    public String set(String key, String value, int expireSecond) {
        Jedis jedis = pool.getResource();
        // 性能监控数据初始化
        final long st = System.nanoTime();
        boolean success = true;
        try {
            if (expireSecond > 0) {
                return jedis.setex(key, expireSecond, value);
            } else {
                //不设置过期时间
                return jedis.set(key, value);
            }
        } catch (Exception e) {
            success = false;
            throw new JedisException(jedis.toString(), e);
        } finally {
            close(jedis);
            metric("setExpire", st, success);
        }
    }

    @Override
    public String get(String key) {
        Jedis jedis = pool.getResource();
        // 性能监控数据初始化
        final long st = System.nanoTime();
        boolean success = true;
        try {
            return jedis.get(key);
        } catch (Exception e) {
            success = false;
            throw new JedisException(jedis.toString(), e);
        } finally {
            close(jedis);
            metric("get", st, success);
        }
    }

    @Override
    public String get(final String key, final int expireSecond) {
        Jedis jedis = pool.getResource();
        // 性能监控数据初始化
        final long st = System.nanoTime();
        boolean success = true;
        try {
            if (expireSecond > 0) {
                // 访LRU,如果命中，则续时，需要指定续时时间
                Pipeline pipelined = jedis.pipelined();
                pipelined.get(key);
                pipelined.expire(key, expireSecond);
                List<Object> results = pipelined.syncAndReturnAll();
                return (String) results.get(0);
            } else {
                return jedis.get(key);
            }
        } catch (Exception e) {
            success = false;
            throw new JedisException(jedis.toString(), e);
        } finally {
            close(jedis);
            metric("getExpire", st, success);
        }
    }

    @Override
    public Long srem(String key, String member) {
        Jedis jedis = pool.getResource();
        // 性能监控数据初始化
        final long st = System.nanoTime();
        boolean success = true;
        try {
            return jedis.srem(key, member);
        } catch (Exception e) {
            success = false;
            throw new JedisException(jedis.toString(), e);
        } finally {
            close(jedis);
            metric("srem", st, success);
        }
    }

    @Override
    public Long sadd(String key, String member) {
        Jedis jedis = pool.getResource();
        // 性能监控数据初始化
        final long st = System.nanoTime();
        boolean success = true;
        try {
            return jedis.sadd(key, member);
        } catch (Exception e) {
            success = false;
            throw new JedisException(jedis.toString(), e);
        } finally {
            close(jedis);
            metric("sadd", st, success);
        }
    }

    @Override
    public <T> void setObject(String key, T value, int expireSeconds) {
        setObject0(key, value, expireSeconds);
    }


    @Override
    public <T> T getObject(String key, final int expireSecond) {
        Jedis jedis = pool.getResource();
        // 性能监控数据初始化
        final long st = System.nanoTime();
        boolean success = true;
        final byte[] keyBytes = getStringBytes(key);
        try {
            byte[] bytes;
            if (expireSecond > 0) {
                // 访LRU,如果命中，则续时，需要指定续时时间
                Pipeline pipelined = jedis.pipelined();
                pipelined.get(keyBytes);
                pipelined.expire(keyBytes, expireSecond);
                List<Object> results = pipelined.syncAndReturnAll();
                bytes = (byte[]) results.get(0);
            } else {
                bytes = jedis.get(keyBytes);
            }
            if (bytes != null) {
                return (T) codec.decode(bytes);
            }
        } catch (Exception e) {
            success = false;
            throw new JedisException(jedis.toString(), e);
        } finally {
            close(jedis);
            metric("getObject", st, success);
        }
        return null;
    }


    @Override
    public <T> List<T> getObjectList(String... keyArray) {
        if (keyArray == null || keyArray.length <= 0) {
            return Collections.emptyList();
        }

        // 获取 Redis 分片
        Jedis jedis = this.pool.getResource();

        // 性能监控数据初始化
        long st = System.nanoTime();
        boolean success = true;

        try {
            // 获取 Redis 管道
            Pipeline pipelined = jedis.pipelined();

            for (String key : keyArray) {
                if (null == key || key.isEmpty()) {
                    continue;
                }

                final byte[] keyByteArray = this.getStringBytes(key);
                pipelined.get(keyByteArray);
            }

            // 获取 Redis 结果列表
            List<Object> redisResultList = pipelined.syncAndReturnAll();

            if (null == redisResultList || redisResultList.isEmpty()) {
                // 如果 Redis 结果为空, 就直接退出!
                return Collections.emptyList();
            }

            List<T> retList = new ArrayList<>(redisResultList.size());

            for (Object redisResult : redisResultList) {
                // 将 Redis 结果转型为字节数组
                byte[] valByteArray = (byte[]) redisResult;

                if (null == valByteArray ||
                        valByteArray.length <= 0) {
                    continue;
                }

                // 解码为对象
                T obj = (T) this.codec.decode(valByteArray);

                if (null != obj) {
                    retList.add(obj);
                }
            }

            return retList;
        } catch (Exception ex) {
            success = false;
            throw new JedisException(ex);
        } finally {
            this.close(jedis);
            metric("getObjectList", st, success);
        }
    }

    @Override
    public List<Object> pipelined(PipelineFunc pipelineFunc) {
        throw new JedisException("no pipelined 方法");
    }
    /**
     * 使用管道处理多个命令
     *
     * @param pipelineFunc
     * @return
     */
    public List<Object> pipelined(String name, Function<Pipeline, Void> pipelineFunc) {
        Jedis jedis = pool.getResource();
        // 性能监控数据初始化
        final long st = System.nanoTime();
        boolean success = true;
        String itemName = name;
        try {
            Pipeline pipelined = jedis.pipelined();
            pipelineFunc.apply(pipelined);
            return pipelined.syncAndReturnAll();
        } catch (Exception e) {
            success = false;
            throw new JedisException(e);
        } finally {
            close(jedis);
            metric(itemName, st, success);
        }
    }

    @Override
    public Object eval(final String key, final String script, final String scriptSHA1, final String... args) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(script));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(key));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(scriptSHA1));
        Jedis jedis = pool.getResource();
        final long st = System.nanoTime();
        boolean success = true;
        final byte[] keyBytes = getStringBytes(key);
        try {
            return evalInOneJedis(jedis, key, script, scriptSHA1, args);
        } catch (Exception e) {
            success = false;
            throw new JedisException(jedis.toString(), e);
        } finally {
            close(jedis);
            metric("eval", st, success);
        }
    }

    @Override
    public <T> T template(Function<ShardedJedis, T> func){
        throw new JedisException("not implement in sentinel");
    }
    /**
     * 提供redis操作的模版方法
     *
     * @param func 操作函数
     * @param <T>  返回的类型
     * @return 返回func执行后的结果
     */
    @Override
    public <T> T templateby(Function<Jedis, T> func) {
        final long st = System.nanoTime();
        boolean success = true;
        Jedis jedis = pool.getResource();
        try {
            return func.apply(jedis);
        } catch (Exception e) {
            success = false;
            throw new JedisException(func.getClass().getName(), e);
        } finally {
            close(jedis);
            metric("template", st, success);
        }
    }

    @Override
    public void processOnAllJedis(Function<Jedis, Future> function) {
        Jedis jedis = pool.getResource();
        try {
            List<Future> futureList = Lists.newArrayList();
            futureList.add(Preconditions.checkNotNull(function.apply(jedis)));

            for (Future future : futureList) {
                try {
                    future.get();
                } catch (Exception e) {
                    LOGGER.error("get futuer result fail", e);
                }
            }
        } finally {
            close(jedis);
        }
    }

    @Override
    public int shards() {
        return 1;
    }

    private <T> void setObject0(final String key, final T value, final int expireSecond) {
        Jedis jedis = pool.getResource();
        // 性能监控数据初始化
        final long st = System.nanoTime();
        boolean success = true;
        final byte[] keyBytes = getStringBytes(key);
        try {
            final byte[] valueBytes = codec.encode(value);
            if (expireSecond > 0) {
                jedis.setex(keyBytes, expireSecond, valueBytes);
            } else {
                //不设置过期时间
                jedis.set(keyBytes, valueBytes);
            }
        } catch (Exception e) {
            success = false;
            throw new JedisException(jedis.toString(), e);
        } finally {
            close(jedis);
            metric("setObject", st, success);
        }
    }


    /**
     * 按照{@link Constants#UTF_8}编码取得byte数组
     *
     * @param str
     * @return
     * @throws RuntimeException
     */
    private byte[] getStringBytes(String str) {
        try {
            return str.getBytes(Constants.UTF_8);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Can't get bytes for [" + str + "] with charset [" + Constants.UTF_8 + "]", e);
        }
    }

    /**
     * 关闭jedis client
     *
     * @param jedis jedis client
     */
    private void close(Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }

    /**
     * @param itemName
     * @param start
     * @param success
     */
    private void metric(String itemName, long start, boolean success) {
        if (enableProbe) {
            redisCallLatency.labels(itemName, success ? "1" : "0").observe((System.nanoTime() - start) / 1.0E9D);
            //oldMetric
            //MetricsProbe.timerUpdateNSFromStart("redis", itemName, start, success);
        }
    }
}


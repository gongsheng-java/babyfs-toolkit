package com.babyfs.tk.service.basic.redis;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import com.babyfs.tk.service.basic.redis.client.PipelineFunc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.exceptions.JedisDataException;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.Future;

/**
 * Redis基础服务封装接口
 * <p/>
 */

public interface IRedis {
    Logger LOGGER = LoggerFactory.getLogger(IRedis.class);

    /**
     * Set操作：获取Set的成员数量
     *
     * @param key
     * @return
     */
    Long scard(final String key);

    String get(String key, int expireSecond);

    /**
     * Set操作：删除String对象
     *
     * @param key
     * @param member
     * @return
     */
    Long srem(String key, String member);

    /**
     * Set操作：增加String对象
     *
     * @param key
     * @param member
     * @return
     */
    Long sadd(String key, String member);

    /**
     * Set操作：增加一个对象成员
     *
     * @param key
     * @param value
     * @param <T>
     * @return
     */
    <T extends Serializable> Long saddObject(String key, T value);

    /**
     * Set操作：查询Set中某成员是否存在
     *
     * @param key
     * @param member
     * @return
     */
    boolean sismember(final String key, final String member);

    /**
     * Set操作：查询Set中某成员是否存在 (Serializable成员 ：Object类型)
     *
     * @param key
     * @param value
     * @param <T>
     * @return
     */
    <T extends Serializable> Boolean sismemberObject(String key, T value);

    /**
     * Set操作：获取所有Set成员（String类型成员）
     *
     * @param key
     * @return
     */
    Set<String> smembers(final String key);

    /**
     * Set操作：获取所有Set成员(Serializable成员 ：Object类型)
     *
     * @param key
     * @param <T>
     * @return
     */
    <T extends Serializable> Set<T> smembersObject(String key);


    /**
     * Map操作：为map中某个key的值incr
     *
     * @param key
     * @param field
     * @param value
     * @return
     */
    Long hincr(String key, String field, long value);

    /**
     * Map操作：为map中某个key的值incr , 带失效时间
     *
     * @param key
     * @param field
     * @param value
     * @param expireSeconds
     * @return
     */
    void hincr(String key, String field, long value, int expireSeconds);

    /**
     * Map操作：获得某个map中的指定数据
     *
     * @param key
     * @param field
     * @return
     */
    String hget(String key, String field);

    /**
     * Map操作：获得某个map中的指定数据
     *
     * @param key
     * @param field
     * @return
     */
    byte[] hget(byte[] key, byte[] field);

    /**
     * Map操作：查看哈希表key中，给定域field是否存在
     *
     * @return
     */
    boolean hexists(String key, String field);

    /**
     * Map操作：获得某个map中所有的数据
     *
     * @param key
     * @return
     */
    Map<String, String> hgetAll(String key);

    /**
     * Map操作: 获取多个field
     *
     * @param key
     * @param fields
     * @return
     */
    List<String> hmget(String key, String... fields);

    /**
     * Map操作：获得哈希表中key对应的所有field
     *
     * @param key
     * @return
     */
    Set<String> hkeys(String key);

    /**
     * Map操作：获得哈希表中key对应的所有values
     *
     * @param key
     * @return
     */
    List<String> hvals(String key);

    /**
     * Map操作：设置某个map中的指定数据
     *
     * @param key
     * @param field
     * @param value
     * @return
     */
    Long hset(String key, String field, String value);

    /**
     * Map操作：设置某个map中的指定数据
     *
     * @param key
     * @param field
     * @param value
     * @return
     */
    Long hset(byte[] key, byte[] field, byte[] value);

    /**
     * Map操作： 返回对应的field的数量
     *
     * @param key
     * @return
     */
    Long hlen(String key);

    /**
     * Map操作：删除哈希表key中的指定域，不存在的域将被忽略
     *
     * @param key
     * @param field
     * @return
     */
    Long hdel(String key, String field);

    /**
     * Map操作：删除哈希表key中的指定域，不存在的域将被忽略
     *
     * @param key
     * @param field
     * @return
     */
    Long hdel(byte[] key, byte[] field);

    /**
     * 删除一个key值的value
     *
     * @param key
     * @return
     */
    Long del(String key);

    /**
     * 添加对象
     *
     * @param key
     * @param value
     * @param expireSeconds 过期时间,单位秒
     * @return
     */
    <T> void setObject(String key, T value, int expireSeconds);

    /**
     * 查询对象
     *
     * @param key
     * @return
     */
    <T> T getObject(String key, final int expireSecond);

    /**
     * 查询对象列表
     *
     * @param keyArray 关键字数组
     * @param <T>      对象类型
     * @return
     */
    <T> List<T> getObjectList(String... keyArray);

    /**
     * 存入或修改String
     *
     * @param key
     * @param value
     * @return
     */
    String set(String key, String value, int expireSeconds);

    /**
     * 查询String
     *
     * @param key
     * @return
     */
    String get(String key);

    /**
     * 判断指定key是否存在
     *
     * @param key
     * @return
     */
    Boolean exists(String key);

    /**
     * 指定字段的值＋1
     *
     * @param key
     * @return
     */
    Long incr(String key);

    /**
     * 指定字段的值＋1，并设置过期时间
     *
     * @param key
     * @param seconds
     * @return
     */
    Long incr(String key, int seconds);

    /**
     * 返回列表长度
     *
     * @param key
     * @return
     */
    Long llen(String key);


    /**
     * 从列表首部插入值
     *
     * @param key
     * @param string
     * @return
     */
    Long rpush(String key, String string);

    /**
     * 从列表尾部插入值
     *
     * @param key
     * @param string
     * @return
     */
    Long lpush(String key, String string);

    /**
     * 取出指定长度的内容，－1表示最后一位，-2表示倒数第二位
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    List<String> lrange(String key, long start, long end);

    /**
     * 获得指定位置的内容
     *
     * @param key
     * @param index
     * @return
     */
    String lindex(String key, long index);

    /**
     * 从列表首部删除一个元素
     *
     * @param key
     * @return
     */
    String lpop(String key);

    /**
     * 丛列表尾部删除一个元素
     *
     * @param key
     * @return
     */
    String rpop(String key);


    /**
     * 对一个列表进行修剪(trim)，就是说，让列表只保留指定区间内的元素，不在指定区间之内的元素都将被删除。
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    String ltrim(String key, long start, long end);

    /**
     * 将列表key下标为index的元素的值甚至为value
     *
     * @param key
     * @param index
     * @param value
     * @return
     */
    String lset(String key, long index, String value);

    /**
     * 根据参数count的值，移除列表中与参数value相等的元素，value为0时候都删除，大于零从头部删除，反之从尾部开始删除
     *
     * @param key
     * @param count
     * @param value
     * @return
     */
    Long lrem(String key, long count, String value);


    /**
     * 设置实效时间
     *
     * @param key
     * @param seconds
     * @return
     */
    Long expire(String key, int seconds);

    /**
     * SortSet操作 ：获取数据成员的索引，按照反排序（最大的成员索引最小）
     *
     * @param key
     * @param member
     * @return
     */
    Long zrevrank(String key, String member);

    /**
     * SortSet操作 ：获取set中成员总数
     *
     * @param key
     * @return
     */
    Long zcard(String key);

    /**
     * 使用管道处理多个命令
     *
     * @param pipelineFunc
     * @return
     */
    List<Object> pipelined(PipelineFunc pipelineFunc);


    /**
     * 根据byte[] key 获得 byte[] 类型数据
     *
     * @param key
     * @return
     */
    byte[] get(byte[] key);

    /**
     * 根据byte[] key 存入 byte[] 类型数据
     *
     * @param key
     * @param value
     * @return
     */
    String set(byte[] key, byte[] value);

    /**
     * 设置基于 byte[] key 的失效时间
     *
     * @param key
     * @param seconds
     * @return
     */
    Long expire(byte[] key, int seconds);

    /**
     * SortSet ： 删除一个成员
     *
     * @param key
     * @param member
     * @return
     */
    Long zrem(String key, String member);

    /**
     * 将指定key的值减1
     *
     * @param key
     * @return
     */
    Long decr(String key);


    /**
     * 将指定key的值减1,并设置过期时间
     *
     * @param key
     * @param seconds
     * @return
     */
    Long decr(String key, int seconds);

    /**
     * 查看指定key的过期时间
     *
     * @param key
     * @return
     */
    Long ttl(String key);

    /**
     * 执行lua脚本
     *
     * @param key
     * @param script     lua script脚本
     * @param scriptSHA1 script的SHA1签名,为了避免重复计算<code>script</code>的sha1值,应该使用{@link Hashing#sha1()}预生成sha1
     * @param args       script需要的参数
     * @return 返回值由script的内容决定
     */
    Object eval(String key, String script, String scriptSHA1, String... args);

    /**
     * 提供redis操作的模版方法
     *
     * @param func 操作函数
     * @param <T>  返回的类型
     * @return 返回func执行后的结果
     */
    <T> T template(Function<ShardedJedis, T> func);

    /**
     * 在所有的redis实例上执行操作
     *
     * @param function not null
     */
    void processOnAllJedis(Function<Jedis, Future> function);

    /**
     * 在一个jedis服务上执行lua脚本
     *
     * @param jedis
     * @param key
     * @param script
     * @param scriptSHA1
     * @param args
     * @return
     */
    default Object evalInOneJedis(Jedis jedis, String key, String script, String scriptSHA1, String... args) {
        ArrayList<String> keys = Lists.newArrayList(key);
        List<String> params = Collections.emptyList();
        if (args != null && args.length > 0) {
            params = Lists.newArrayList(args);
        }
        try {
            return jedis.evalsha(scriptSHA1, keys, params);
        } catch (JedisDataException e) {
            String message = e.getMessage();
            if (message != null && message.contains("Please use EVAL")) {
                String toCheckSHA1 = Hashing.sha1().hashString(script, Charsets.UTF_8).toString();
                Preconditions.checkArgument(scriptSHA1.equals(toCheckSHA1), "scriptSHA1 %s not equals %s", scriptSHA1, toCheckSHA1);
                LOGGER.warn("Can't find script for sha1 {},use eval instead of", scriptSHA1);
                return jedis.eval(script, keys, params);
            } else {
                throw e;
            }
        }
    }

}

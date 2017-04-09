package com.babyfs.tk.service.biz.counter.impl;

import com.alibaba.fastjson.JSON;
import com.babyfs.tk.commons.base.Pair;
import com.babyfs.tk.service.basic.INameResourceService;
import com.babyfs.tk.service.basic.redis.IRedis;
import com.babyfs.tk.service.basic.redis.client.PipelineFunc;
import com.babyfs.tk.service.biz.cache.CacheParameter;
import com.babyfs.tk.service.biz.counter.ITemporalCounterService;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.ShardedJedisPipeline;

import javax.annotation.Nullable;
import java.util.*;
import com.babyfs.tk.service.biz.counter.CounterConst;
import static com.babyfs.tk.service.biz.counter.CounterConst.*;


/**
 * 使用redis实现的,不需要持久化的计数器服务,用户非关键的业务
 * <p>
 * 计数器的redis数据结构为hash,key的格式为{@linkplain #counterCacheParameter}getRedisKeyPrefix:`type`_`id`,hash的结构定义如下:
 * <pre>
 * {
 *  field1:value, // field1的计数值
 *  field2:value, // field2的计数值
 * ...
 * }
 * </pre>
 * field的名称不能以{@link CounterConst#INTERNAL_PREFIX}开头,{@value CounterConst#INTERNAL_PREFIX}开头的field约定为由系统内部使用.
 * </p>
 */
public class RedisTemporalCounterService implements ITemporalCounterService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisTemporalCounterService.class);

    private final INameResourceService<IRedis> cacheServcie;
    /**
     * 计数器的cache配置
     */
    private final CacheParameter counterCacheParameter;

    /**
     * @param counterCacheParameter
     * @param cacheServcie
     */
    public RedisTemporalCounterService(CacheParameter counterCacheParameter, INameResourceService<IRedis> cacheServcie) {
        this.cacheServcie = Preconditions.checkNotNull(cacheServcie);
        this.counterCacheParameter = Preconditions.checkNotNull(counterCacheParameter);
    }

    /**
     * @param type          计数器的类型,>0
     * @param id            计数器的id,非空
     * @param fieldAndDelta field及其增量,非空
     * @return
     */
    @Override
    @SafeVarargs
    public final boolean incr(final int type, final String id, final Pair<String, Long>... fieldAndDelta) {
        Preconditions.checkArgument(type > 0, "type must >0");
        Preconditions.checkNotNull(id, "id must not be null");
        Preconditions.checkArgument(fieldAndDelta != null && fieldAndDelta.length > 0, "fieldAndDelta must not be null");
        Optional<Pair<String, Long>> optional = Arrays.stream(fieldAndDelta).filter(pair -> pair.first.startsWith(CounterConst.INTERNAL_PREFIX)).findFirst();
        if (optional.isPresent()) {
            throw new IllegalArgumentException("invalid field `" + optional.get().first + "`");
        }

        try {
            final IRedis redis = getRedis();
            final String counterKey = getCounterCacheKey(type, id);
            final int redisExpireSecond = this.counterCacheParameter.getRedisExpireSecond();
            redis.pipelined(new PipelineFunc() {
                @Nullable
                @Override
                public Void apply(@Nullable ShardedJedisPipeline jedisPipeline) {
                    if (jedisPipeline == null) {
                        throw new NullPointerException("invalid jedis pipeline");
                    }

                    for (Pair<String, Long> fp : fieldAndDelta) {
                        jedisPipeline.hincrBy(counterKey, fp.first, fp.second);
                    }

                    if (redisExpireSecond > 0) {
                        jedisPipeline.expire(counterKey, redisExpireSecond);
                    }
                    return null;
                }
            });
            return true;
        } catch (Exception e) {
            LOGGER.error("incr fail,type:" + type + ",id:" + id + JSON.toJSONString(fieldAndDelta), e);
        }
        return false;
    }

    @Override
    public void del(final int type, final String id) {
        Preconditions.checkArgument(type > 0, "type must >0");
        Preconditions.checkNotNull(id, "id must not be null");

        final IRedis redis = getRedis();
        final String counterKey = getCounterCacheKey(type, id);
        redis.del(counterKey);
    }

    @Override
    public void delFields(int type, String id, String... fields) {
        Preconditions.checkArgument(type > 0, "type must >0");
        Preconditions.checkNotNull(id, "id must not be null");
        Preconditions.checkArgument(fields != null && fields.length > 0, "fields length must >0");

        final IRedis redis = getRedis();
        final String counterKey = getCounterCacheKey(type, id);
        redis.pipelined(new PipelineFunc() {
            @Nullable
            @Override
            public Void apply(@Nullable ShardedJedisPipeline jedisPipeline) {
                if (jedisPipeline == null) {
                    throw new NullPointerException("invalid jedis pipeline");
                }
                jedisPipeline.hdel(counterKey, fields);
                return null;
            }
        });
    }

    @Override
    public Map<String, Long> getAll(final int type, final String id) {
        Preconditions.checkArgument(type > 0, "type must >0");
        Preconditions.checkNotNull(id, "id must not be null");

        final IRedis redis = getRedis();
        final String counterKey = getCounterCacheKey(type, id);
        final Map<String, String> allFields = redis.hgetAll(counterKey);

        if (allFields.isEmpty()) {
            return Collections.emptyMap();
        } else {
            return buildCounterFromRedisFields(allFields);
        }
    }

    @Override
    public Map<String, Long> getFields(int type, String id, String... fields) {
        Preconditions.checkArgument(type > 0, "type must >0");
        Preconditions.checkNotNull(id, "id must not be null");
        Preconditions.checkArgument(fields != null && fields.length > 0, "fields length must >0");

        final IRedis redis = getRedis();
        final String counterKey = getCounterCacheKey(type, id);
        List<String> strings = redis.hmget(counterKey, fields);

        final Map<String, String> allFields = Maps.newHashMap();
        for (int i = 0; i < fields.length; i++) {
            String key = fields[i];
            String value = strings.get(i);
            if (!Strings.isNullOrEmpty(value)) {
                allFields.put(key, value);
            }
        }
        return buildCounterFromRedisFields(allFields);
    }

    public INameResourceService<IRedis> getCacheServcie() {
        return cacheServcie;
    }

    public IRedis getRedis() {
        final String redisServiceGroup = this.counterCacheParameter.getRedisServiceGroup();
        try {
            return cacheServcie.get(redisServiceGroup);
        } catch (Exception e) {
            LOGGER.error("can't get redis for group:`" + redisServiceGroup + "`", e);
            throw new RuntimeException(e);
        }
    }

    private String getCounterCacheKey(int type, String id) {
        return buildCounterKey(this.counterCacheParameter.getRedisKeyPrefix(), type, id);
    }
}

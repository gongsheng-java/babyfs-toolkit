package com.babyfs.tk.service.biz.counter;

import com.babyfs.tk.commons.base.Pair;
import com.babyfs.tk.service.basic.redis.LuaScript;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

/**
 * 定义计数器相关的Lua脚本
 */
public final class CounterConst {
    private static final Logger LOGGER = LoggerFactory.getLogger(CounterConst.class);

    public static final String INTERNAL_PREFIX = "_";

    public static final String SHARD_HASH_KEY_TEMPLATE = "{%d}";

    public static final String SHARD_HASH_TAG = "}";

    public static final LuaScript COUNTER_UPDATE_LUA = LuaScript.createRedisLuaScript("lua/counter_update.lua");
    public static final LuaScript COUNTER_SET_SYNC_LUA = LuaScript.createRedisLuaScript("lua/counter_update_sync.lua");
    public static final LuaScript COUNTER_EVICT_LUA = LuaScript.createRedisLuaScript("lua/counter_evict.lua");
    public static final LuaScript COUNTER_HGETALL_LUA = LuaScript.createRedisLuaScript("lua/counter_getall.lua");
    public static final LuaScript COUNTER_DEL_LUA = LuaScript.createRedisLuaScript("lua/counter_del.lua");

    /**
     * 计数器时间戳的起始时间,单位秒
     *
     * @see {@link #counterEpochSecond()}
     */
    public static final long COUNTER_EPOCH_SECOND = ZonedDateTime.of(2009, 11, 20, 0, 0, 0, 0, ZoneOffset.UTC).toEpochSecond();

    /**
     * 计数器的写入版本号
     */
    public static final String COUNTER_WRITE_VERSION = "_w";
    /**
     * 计数器的同步版本号
     */
    public static final String COUNTER_SYNC_VERSION = "_s";
    /**
     * 计数器的同步时间戳
     */
    public static final String COUNTER_SYNC_TIMESTAMP = "_st";

    /**
     * 计算counter的从{@link #COUNTER_EPOCH_SECOND}开始的时间戳,单位秒
     *
     * @return
     */
    public static long counterEpochSecond() {
        return counterEpochSecond(System.currentTimeMillis());
    }

    /**
     * timeMillis距离{@link #COUNTER_EPOCH_SECOND}开始的时间戳,单位秒
     *
     * @param timeMillis 单位毫秒
     * @return
     */
    public static long counterEpochSecond(long timeMillis) {
        Preconditions.checkArgument(timeMillis > 0);
        final long epochSecond = timeMillis / 1000 - COUNTER_EPOCH_SECOND;
        if (epochSecond < 0) {
            throw new IllegalArgumentException("Invalid time millis");
        }
        return epochSecond;
    }

    /**
     * 真实的UTC时间
     *
     * @param counterEpochSecond
     * @return
     */
    public static long realCounterEpochSecond(long counterEpochSecond) {
        return counterEpochSecond + COUNTER_EPOCH_SECOND;
    }

    /**
     * 构建counter key
     *
     * @param prefix key的前缀,前缀中不能包含`:`,非空空
     * @param type   counter的类型
     * @param id
     * @return
     */
    public static String buildCounterKey(String prefix, int type, String id, int shards) {
        prefix = Preconditions.checkNotNull(StringUtils.trimToNull(prefix));
        id = Preconditions.checkNotNull(StringUtils.trimToNull(id));
        Preconditions.checkArgument(!prefix.contains(":"), "prefix can't contain `:`");
        // Preconditions.checkArgument(shards <= 0, "shards must > 0");

        // 将id取模，作为hashKey，使用该hash保证counterkey和synckey分在同一个redis shard
        if (shards > 0) {
            String key = String.format(SHARD_HASH_KEY_TEMPLATE, id.hashCode() & (shards - 1)) + prefix + "h:" + type + INTERNAL_PREFIX + id;
            LOGGER.info("rediscounterkey: prex {},id {}, shareds:{}, result:{}",SHARD_HASH_KEY_TEMPLATE,id.hashCode() & (shards - 1),shards,key);
            return key;
        } else {
            return prefix + "h:" + type + INTERNAL_PREFIX + id;
        }
    }

    /**
     * 从counter key中解析counter的类型和id
     *
     * @param counterKey 非空
     * @return
     */
    public static Pair<Integer, String> parseCounterTypeAndId(String counterKey) {
        counterKey = Preconditions.checkNotNull(StringUtils.trimToNull(counterKey));
        List<String> strings = Splitter.on(":").trimResults().omitEmptyStrings().splitToList(counterKey);
        Preconditions.checkArgument(strings.size() == 2, "invalid counter key:`" + counterKey + "`");
        List<String> typeAndId = Splitter.on(INTERNAL_PREFIX).limit(2).trimResults().omitEmptyStrings().splitToList(strings.get(1));
        return Pair.of(Integer.parseInt(typeAndId.get(0)), typeAndId.get(1));
    }

    /**
     * 从counterKey中解析hash片段，如countkey 是{1}co_co_1, 将返回{1}
     *
     * @param counterKey
     * @return
     */
    public static String getShardHashKey(String counterKey) {
        counterKey = Preconditions.checkNotNull(StringUtils.trimToNull(counterKey));
        int hashTagEnd = counterKey.indexOf(SHARD_HASH_TAG);
        if (hashTagEnd > 1) {
            return counterKey.substring(0, hashTagEnd + 1);
        }
        return "";
    }

    /**
     * 从redis field中构建计数器
     *
     * @param fields
     * @return
     */
    public static Map<String, Long> buildCounterFromRedisFields(Map<String, String> fields) {
        if (fields == null) {
            return null;
        }

        final Map<String, Long> counterFields = Maps.newHashMap();
        for (Map.Entry<String, String> kv : fields.entrySet()) {
            String field = kv.getKey();
            String value = kv.getValue();
            if (!field.startsWith(INTERNAL_PREFIX)) {
                counterFields.put(field, Long.parseLong(value));
            }
        }
        return counterFields;
    }
}

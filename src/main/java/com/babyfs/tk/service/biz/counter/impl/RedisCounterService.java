package com.babyfs.tk.service.biz.counter.impl;

import com.alibaba.fastjson.JSON;
import com.babyfs.tk.commons.base.Pair;
import com.babyfs.tk.service.basic.INameResourceService;
import com.babyfs.tk.service.basic.redis.IRedis;
import com.babyfs.tk.service.basic.redis.LuaScript;
import com.babyfs.tk.service.biz.cache.CacheParameter;
import com.babyfs.tk.service.biz.counter.ICounterPersistService;
import com.babyfs.tk.service.biz.counter.ICounterService;
import com.babyfs.tk.service.biz.counter.CounterConst;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.util.Hashing;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.babyfs.tk.service.biz.counter.CounterConst.*;


/**
 * 使用redis实现的,需要持久化的计数器服务,维护计数服务及计数器key的同步集合(sync set).
 * <p>
 * 计数器的redis数据结构为hash,key的格式为{@linkplain #counterCacheParameter}getRedisKeyPrefix:`type`_`id`,hash的结构定义如下:
 * <pre>
 * {
 *  _w:version, //写入的版本号
 *  _s:version, //同步的版本号,来自_w某个时刻的值
 *  _st:timestamp,//最后一次同步的时间戳
 *  field1:value, // field1的计数值
 *  field2:value, // field2的计数值
 * ...
 * }
 * </pre>
 * field的名称不能以{@link CounterConst#INTERNAL_PREFIX}开头,{@value CounterConst#INTERNAL_PREFIX}开头的field约定为由系统内部使用.
 * </p>
 * <p>
 * 计数器的更新和初始化使用lua脚本,脚本定义在{@link CounterConst#COUNTER_UPDATE_LUA}.
 * </p>
 * <p>
 * <b>{@link RedisCounterService}和{@link RedisCounterSyncService}实例协作使用,前者记录计数的同步集合,后者扫描同步集合,并淘汰
 * </p>
 */
public class RedisCounterService implements ICounterService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisCounterService.class);


    private final INameResourceService<IRedis> cacheServcie;
    /**
     * 计数器持久化服务
     */
    private final ICounterPersistService counterPersistService;
    /**
     * 计数器的cache配置
     */
    private final CacheParameter counterCacheParameter;

    /**
     * 每个redis实例上sync set的桶slot个数,> 0
     */
    private final int slots;

    /**
     * counter的名称
     */
    private final String name;


    /**
     * @param name                  not null
     * @param counterCacheParameter
     * @param counterPersistService
     * @param slots                 >0
     * @param cacheServcie
     */
    public RedisCounterService(String name, CacheParameter counterCacheParameter, ICounterPersistService counterPersistService, int slots, INameResourceService<IRedis> cacheServcie) {
        this.name = Preconditions.checkNotNull(name);
        this.cacheServcie = Preconditions.checkNotNull(cacheServcie);
        this.counterCacheParameter = Preconditions.checkNotNull(counterCacheParameter);
        this.counterPersistService = Preconditions.checkNotNull(counterPersistService);
        Preconditions.checkArgument(slots > 0, "slots >0");
        this.slots = slots;
    }

    /**
     * @param type          计数器的类型,>0
     * @param id            计数器的id,非空
     * @param fieldAndDelta field及其增量,非空
     * @return
     */
    @Override
    public boolean incr(final int type, final String id, final List<Pair<String, Long>> fieldAndDelta) {
        Preconditions.checkArgument(type > 0, "type must >0");
        Preconditions.checkNotNull(id, "id must not be null");
        Preconditions.checkArgument(fieldAndDelta != null && fieldAndDelta.size() > 0, "fieldAndDelta must not be null");
        Optional<Pair<String, Long>> optional = fieldAndDelta.stream().filter(pair -> pair.first.startsWith(INTERNAL_PREFIX)).findFirst();
        if (optional.isPresent()) {
            throw new IllegalArgumentException("invalid field `" + optional.get().first + "`");
        }

        try {
            final IRedis redis = getRedis();
            final String counterKey = getCounterCacheKey(type, id);
            final String syncSetKey = getSyncSetSlotKeyForCounter(counterKey);

            int exist = LuaScript.LUA_FALSE;
            int updated = LuaScript.LUA_FALSE;

            // 直接进行更新
            {
                final String[] args = buildUpdateArgs(syncSetKey, LuaScript.LUA_FALSE, fieldAndDelta);

                //LUA:更新计数器，将counter_key加入到sync set中
                final List updateResult = (List) redis.eval(counterKey, CounterConst.COUNTER_UPDATE_LUA.getScript(), CounterConst.COUNTER_UPDATE_LUA.getSha1(), args);
                exist = ((Number) updateResult.get(0)).intValue();
                updated = ((Number) updateResult.get(1)).intValue();

                if (LuaScript.isTrue(updated)) {
                    return true;
                }
            }

            //key不存在,从持久层查询取数据后进行初始化,同时进行更新
            if (LuaScript.isFalse(exist)) {
                final List<Pair<String, Long>> initFields = buildInitFields(counterPersistService.get(type, id));
                List<Pair<String, Long>> allArgsList = Lists.newArrayList(fieldAndDelta);
                allArgsList.addAll(initFields);

                final String[] initAndUpdateArgs = buildUpdateArgs(syncSetKey, LuaScript.LUA_TRUE, allArgsList);
                //LUA:初始化计数器，将counter_key加入到sync set中
                final List initAndUpdateResult = (List) redis.eval(counterKey, CounterConst.COUNTER_UPDATE_LUA.getScript(), CounterConst.COUNTER_UPDATE_LUA.getSha1(), initAndUpdateArgs);
                updated = ((Number) initAndUpdateResult.get(1)).intValue();
                return LuaScript.isTrue(updated);
            }
        } catch (Exception e) {
            LOGGER.error("incr fail,type:" + type + ",id:" + id + JSON.toJSONString(fieldAndDelta), e);
        }
        return false;
    }

    /**
     * 同步计数器
     *
     * @param type >0
     * @param id   非空
     * @return 同步是否成功
     */
    public boolean syncCounter(int type, String id) {
        Preconditions.checkArgument(type > 0);
        Preconditions.checkNotNull(id);

        final IRedis counterRedis = getRedis();
        final String counterKey = this.getCounterCacheKey(type, id);

        Map<String, String> fields = counterRedis.hgetAll(counterKey);
        if (fields == null || fields.isEmpty()) {
            LOGGER.warn("can't find counter key:{}", counterKey);
            return false;
        }

        long writeVersion = Long.parseLong(fields.get(COUNTER_WRITE_VERSION));
        Map<String, Long> counterFields = CounterConst.buildCounterFromRedisFields(fields);
        boolean synced = counterPersistService.sync(type, id, counterFields);
        if (synced) {
            //LUA:更新last_sync
            counterRedis.eval(counterKey, COUNTER_SET_SYNC_LUA.getScript(), COUNTER_SET_SYNC_LUA.getSha1(), String.valueOf(writeVersion), String.valueOf(counterEpochSecond()));
        }
        return synced;
    }


    @Override
    public Map<String, Long> get(final int type, final String id) {
        Preconditions.checkArgument(type > 0, "type must >0");
        Preconditions.checkNotNull(id, "id must not be null");

        final long lastAccessTime = counterEpochSecond();
        final IRedis redis = getRedis();
        final String counterKey = getCounterCacheKey(type, id);
        final String syncSetKey = getSyncSetSlotKeyForCounter(counterKey);

        //LUA:取得计数其的值,并更新conter_key在sync set中的访问时间
        @SuppressWarnings("unchecked")
        List<String> ret = (List<String>) redis.eval(counterKey, COUNTER_HGETALL_LUA.getScript(), COUNTER_HGETALL_LUA.getSha1(), syncSetKey, String.valueOf(lastAccessTime));

        final Map<String, String> allFields = Maps.newHashMap();
        for (int i = 0; i < ret.size(); i += 2) {
            allFields.put(ret.get(i), ret.get(i + 1));
        }

        if (allFields.isEmpty()) {
            //redis中没有找到值,从持久层取得数据后初始化
            Map<String, Long> origin = counterPersistService.get(type, id);

            //LUA:初始化计数器
            redis.eval(counterKey, CounterConst.COUNTER_UPDATE_LUA.getScript(), CounterConst.COUNTER_UPDATE_LUA.getSha1(), buildUpdateArgs(syncSetKey, LuaScript.LUA_TRUE, buildInitFields(origin)));
            return origin;
        } else {
            return buildCounterFromRedisFields(allFields);
        }
    }


    @Override
    public void del(final int type, final String id) {
        Preconditions.checkArgument(type > 0, "type must >0");
        Preconditions.checkNotNull(id, "id must not be null");
        final IRedis redis = getRedis();
        final String counterKey = getCounterCacheKey(type, id);
        final String syncSetKey = getSyncSetSlotKeyForCounter(counterKey);

        //LUA: 同时删除counter_key和sync set中国年的conter key
        redis.eval(counterKey, COUNTER_DEL_LUA.getScript(), COUNTER_DEL_LUA.getSha1(), syncSetKey);

        counterPersistService.del(type, id);
    }

    @Override
    public void delOnlyCache(final int type, final String id) {
        Preconditions.checkArgument(type > 0, "type must >0");
        Preconditions.checkNotNull(id, "id must not be null");

        final IRedis redis = getRedis();
        final String counterKey = getCounterCacheKey(type, id);
        final String syncSetKey = getSyncSetSlotKeyForCounter(counterKey);

        redis.eval(counterKey, COUNTER_DEL_LUA.getScript(), COUNTER_DEL_LUA.getSha1(), syncSetKey);
    }

    /**
     * 取得计数器key对应的同步集合
     *
     * @param counterKey
     * @return
     */
    public String getSyncSetSlotKeyForCounter(String counterKey) {
        final long counterKeyHash = Math.abs(Hashing.MURMUR_HASH.hash(counterKey));
        final long counterKeySlot = counterKeyHash % slots;
        final String shardHash = getShardHashKey(counterKey);
        return getSyncSetSlotKey(counterKeySlot, shardHash);
    }

    /**
     * 取得sorted set分桶的key
     *
     * @param slotIndex
     * @return
     */
    public String getSyncSetSlotKey(long slotIndex, String shardHash) {
        return shardHash + counterCacheParameter.getRedisKeyPrefix() + "z.sync:" + slotIndex;
    }

    public String getName() {
        return name;
    }

    public int getSlots() {
        return slots;
    }

    public INameResourceService<IRedis> getCacheServcie() {
        return cacheServcie;
    }

    public ICounterPersistService getCounterPersistService() {
        return counterPersistService;
    }

    public CacheParameter getCounterCacheParameter() {
        return counterCacheParameter;
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
        return buildCounterKey(this.counterCacheParameter.getRedisKeyPrefix(), type, id, getRedis().shards());
    }

    private List<Pair<String, Long>> buildInitFields(Map<String, Long> origin) {
        final List<Pair<String, Long>> fieldAndValueList = Lists.newArrayListWithCapacity(origin != null ? origin.size() : 0);
        if (origin != null) {
            //用于初始化的字段以INTERNAL_PREFIX开头,由Lua脚本识别,去掉前缀后作为hash初始化字段的名称
            for (Map.Entry<String, Long> pair : origin.entrySet()) {
                fieldAndValueList.add(Pair.of(INTERNAL_PREFIX + pair.getKey(), pair.getValue()));
            }
        }
        return fieldAndValueList;
    }

    /**
     * 构建更新的参数列表
     *
     * @param syncSetKey    同步集合的key
     * @param isInit        是否包含初始化操作
     * @param fieldAndDelta 字段及其计数
     * @return
     */
    private static String[] buildUpdateArgs(String syncSetKey, int isInit, List<Pair<String, Long>> fieldAndDelta) {
        Preconditions.checkNotNull(syncSetKey);
        Preconditions.checkArgument(fieldAndDelta.size() > 0, "Invalid field and delta");
        String[] args = new String[3 + fieldAndDelta.size() * 2];
        int index = 0;
        args[index++] = syncSetKey;
        args[index++] = String.valueOf(counterEpochSecond());
        args[index++] = String.valueOf(isInit);
        for (int i = 0; i < fieldAndDelta.size(); i++) {
            Pair<String, Long> pair = fieldAndDelta.get(i);
            args[index + i * 2] = pair.first;
            args[index + i * 2 + 1] = pair.second.toString();
        }
        return args;
    }
}

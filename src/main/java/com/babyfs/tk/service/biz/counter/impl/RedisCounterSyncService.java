package com.babyfs.tk.service.biz.counter.impl;

import com.babyfs.tk.commons.base.Pair;
import com.babyfs.tk.service.basic.INameResourceService;
import com.babyfs.tk.service.basic.redis.IRedis;
import com.babyfs.tk.service.basic.redis.LuaScript;
import com.babyfs.tk.service.biz.cache.CacheParameter;
import com.babyfs.tk.service.biz.counter.ICounterPersistService;
import com.babyfs.tk.service.biz.counter.CounterConst;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Client;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import static com.babyfs.tk.service.biz.counter.CounterConst.*;

/**
 * 扫描{@link RedisCounterService}记录的同步集合(sync set),根据策略进行同步和淘汰:
 * <p>
 * <p>1. 扫描set,根据策略定时同步计数器到持久层</p>
 * <p>2. 扫描set,根据策略淘汰redis中的计数器,淘汰前进行同步</p>
 * <p>
 * </p>
 */
public class RedisCounterSyncService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisCounterSyncService.class);
    private final RedisCounterService redisCounterService;
    /**
     * 计数器持久化服务
     */
    private final ICounterPersistService counterPersistService;
    private final INameResourceService<IRedis> cacheServcie;
    /**
     * 同步集合的cache配置
     */
    private final CacheParameter syncSetCacheParameter;
    /**
     * 每个slot的最大元素个数,超出的将会被删除
     */
    private final long slotMaxItems;
    /**
     * 同步版本号与写入版本号的最小差距,即write_version - sync_version >= minSyncVersionChanges时,即触发同步
     */
    private final long minSyncVersionChanges;
    /**
     * 同步时间戳与当前时间的最小差距,即now_second - sync_time >= minSyncInerval,即触发同步
     */
    private final long minSyncIntervalSecond;
    /**
     * 根据访问时间的淘汰间隔,单位秒
     */
    private final long evictIntervalSecond;
    /**
     * 是否停止
     */
    private volatile boolean stop = false;

    /**
     * @param cacheServcie          not null
     * @param syncSetCacheParameter 同步集合的cache配置,该配置应该是直连redis实例,即redis不能连接类似twmporxy的代理
     * @param redisCounterService   not null
     * @param slotMaxItems          每个桶中的最大元素个数,>0
     * @param minSyncVersionChanges 同步版本号与写入版本号的差距,>0
     * @param minSyncIntervalSecond 同步时间戳与当前时间的最小差距,>0
     * @param evictIntervalSecond   淘汰的时间间隔,单位秒,>0
     */
    public RedisCounterSyncService(INameResourceService<IRedis> cacheServcie,
                                   CacheParameter syncSetCacheParameter,
                                   RedisCounterService redisCounterService,
                                   long slotMaxItems,
                                   long minSyncVersionChanges,
                                   long minSyncIntervalSecond,
                                   long evictIntervalSecond) {
        this.cacheServcie = Preconditions.checkNotNull(cacheServcie);
        this.redisCounterService = Preconditions.checkNotNull(redisCounterService);
        this.counterPersistService = Preconditions.checkNotNull(redisCounterService.getCounterPersistService());
        this.syncSetCacheParameter = Preconditions.checkNotNull(syncSetCacheParameter);

        Preconditions.checkArgument(slotMaxItems > 0, "slots >0");
        Preconditions.checkArgument(minSyncVersionChanges > 0, "minSyncVersionChanges >0");
        Preconditions.checkArgument(minSyncIntervalSecond > 0, "minSyncIntervalSecond >0");
        Preconditions.checkArgument(evictIntervalSecond > 0, "evict >0");

        this.slotMaxItems = slotMaxItems;
        this.minSyncVersionChanges = minSyncVersionChanges;
        this.minSyncIntervalSecond = minSyncIntervalSecond;
        this.evictIntervalSecond = evictIntervalSecond;
        LOGGER.info("sync set redis group :{},slots:{},slot max items:{},sync interval:{}s,evict interval:{}s",
                syncSetCacheParameter.getRedisServiceGroup(), redisCounterService.getSlots(), slotMaxItems, minSyncVersionChanges, evictIntervalSecond);
    }

    /**
     * 扫描并同步所有的redis实例上的sync_set
     */
    public void scanAll() {
        IRedis redis = this.getSyncSetRedis();
        redis.processOnAllJedis(new Function<Jedis, Future>() {
            @Nullable
            @Override
            public Future apply(@Nullable Jedis input) {
                Preconditions.checkNotNull(input);
                FutureTask task = new FutureTask<>(() -> {
                    final Client client = input.getClient();
                    LOGGER.info("begin scan redis {}:{},db:{}", client.getHost(), client.getPort(), client.getDB());
                    for (int i = 0; i < redisCounterService.getSlots(); i++) {
                        scan(redis, input, i);
                    }
                    LOGGER.info("finish scan redis {}:{},db:{}", client.getHost(), client.getPort(), client.getDB());
                    return null;
                });
                task.run();
                return task;
            }
        });
    }

    /**
     * 在execturo中执行扫描并同步所有的redis实例上的sync_set
     *
     * @param executorService not null
     */
    public void scanAll(ExecutorService executorService) {
        IRedis redis = this.getSyncSetRedis();
        redis.processOnAllJedis(new Function<Jedis, Future>() {
            @Nullable
            @Override
            public Future apply(@Nullable Jedis input) {
                Callable<Void> callable = () -> {
                    for (int i = 0; i < redisCounterService.getSlots(); i++) {
                        scan(redis, input, i);
                    }
                    return null;
                };
                return executorService.submit(callable);
            }
        });
    }

    /**
     * 扫描指定的slot
     *
     * @param wrapper
     * @param jedis
     * @param slotIndex slotIndex &gt;=0 and < {@link RedisCounterService#getSlots()}
     */
    private void scan(IRedis wrapper, Jedis jedis, int slotIndex) {
        Preconditions.checkArgument(slotIndex >= 0 && slotIndex < redisCounterService.getSlots(), "Invalid slotIndex:%d", slotIndex);

        if (stop) {
            LOGGER.info("stop {},skip", stop);
            return;
        }

        final String syncSetSlotKey = redisCounterService.getSyncSetSlotKey(slotIndex);
        final long originLength = jedis.zcard(syncSetSlotKey);
        final long overItems = originLength - this.slotMaxItems;

        long needEvictItemCount = 0;
        if (overItems > 0) {
            needEvictItemCount = overItems;
        }

        LOGGER.info("begin scan slotKey:{},slotIndex length:{},needEvictItemCount:{}", syncSetSlotKey, originLength, needEvictItemCount);
        if (originLength <= 0) {
            return;
        }

        final long st = System.currentTimeMillis();
        long syncedCount = 0;
        long evictedCount = 0;
        long processedCount = 0;

        final int batch = 30;
        final long maxProcessedCount = 2 * originLength; //最大的扫描个数

        // 按照access time 升序分批次处理
        int batchRemoved = 0; //每轮批量处理删除的个数
        for (int nextStart = 0; ; ) {
            if (stop) {
                LOGGER.info("stop {},skip", stop);
                break;
            }

            if (processedCount >= maxProcessedCount) {
                LOGGER.warn("scan slotKey:{},processedCount {} >= 2 * originLength({}),skip it", syncSetSlotKey, processedCount, maxProcessedCount);
            }
            int start = nextStart;
            //如果上一批次处理有删除元素,则修复本批次的起始和结束位置
            if (batchRemoved > 0) {
                start = nextStart - batchRemoved;
                if (start < 0) {
                    start = 0;
                }
            }
            int end = start + batch - 1;
            nextStart = end + 1;

            if (start >= jedis.zcard(syncSetSlotKey)) {
                break;
            }

            LOGGER.debug("fetch {},start:{},end:{},nexStart:{},pre batch removed:{}", syncSetSlotKey, start, end, nextStart, batchRemoved);

            batchRemoved = 0;
            Set<Tuple> counterKeyAndTimeSet = jedis.zrangeWithScores(syncSetSlotKey, start, end);
            LOGGER.debug("fetch {},start:{},end:{},count:{}", syncSetSlotKey, start, end, counterKeyAndTimeSet.size());

            if (counterKeyAndTimeSet.isEmpty()) {
                break;
            }

            for (Tuple tuple : counterKeyAndTimeSet) {
                processedCount++;
                final String counterKey = tuple.getElement();
                final long accessTime = (long) tuple.getScore();
                if (Strings.isNullOrEmpty(counterKey)) {
                    continue;
                }

                if (stop) {
                    LOGGER.info("stop {},skip", stop);
                    break;
                }

                try {
                    //取得counter key的最后一次写入的版本号和同步的版本号
                    Map<String, String> fields = jedis.hgetAll(counterKey);
                    if (fields == null || fields.isEmpty()) {
                        LOGGER.warn("not exist counter key:{},remove it from sync set:{}", counterKey, syncSetSlotKey);
                        if (jedis.zrem(syncSetSlotKey, counterKey) > 0) {
                            batchRemoved++;
                        }
                        continue;
                    }

                    String lastWrite = fields.get(CounterConst.COUNTER_WRITE_VERSION);
                    String lastSync = fields.get(CounterConst.COUNTER_SYNC_VERSION);
                    String syncTime = fields.get(CounterConst.COUNTER_SYNC_TIMESTAMP);

                    if (Strings.isNullOrEmpty(lastWrite) || Strings.isNullOrEmpty(lastSync) || Strings.isNullOrEmpty(syncTime)) {
                        LOGGER.warn("invalid counter key:{},_w:{},_s:{},_st:{},remove it from sync set:{}", counterKey, lastWrite, lastSync, syncTime, syncSetSlotKey);
                        if (jedis.zrem(syncSetSlotKey, counterKey) > 0) {
                            batchRemoved++;
                        }
                        continue;
                    }

                    final long writeVersion = Long.parseLong(lastWrite);
                    final long syncVersion = Long.parseLong(lastSync);
                    final long syncEcpochSecond = Long.parseLong(syncTime);
                    final long nowSecond = CounterConst.counterEpochSecond();

                    /*
                     1. 检查写入版本号和同步版本号,如同步版本号小于写入版本号:
                        1)write_version - sync_version >= minSyncVersionChanges时,需要同步
                        2)nowSecond - syncEpochSecond  >= minSyncIntervalSecond,需要同步
                     2. 检查访问时间,检查是否需要淘汰
                     3. 根据元素的个数,决定需要淘汰的个数
                    */
                    boolean needSync = false;
                    boolean needEvict = false;

                    //检查是否需要同步
                    if (writeVersion > 0) {
                        if (syncVersion < writeVersion) {
                            final long lastSyncInterval = nowSecond - syncEcpochSecond;
                            if (lastSyncInterval >= this.minSyncIntervalSecond || writeVersion - syncVersion >= this.minSyncVersionChanges) {
                                needSync = true;
                            }
                        }
                    }

                    //根据访问时间检查是否需要淘汰
                    if (accessTime > 0) {
                        if (nowSecond - accessTime >= this.evictIntervalSecond) {
                            LOGGER.debug("evict,slot:{}, counter key:{},access time:{}", syncSetSlotKey, counterKey, accessTime);
                            needEvict = true;
                        }
                    }

                    if (needEvictItemCount > 0) {
                        LOGGER.debug("evict,slot:{},counter key:{},needEvictItemCount:{}", syncSetSlotKey, counterKey, needEvictItemCount);
                        needEvict = true;
                    }

                    if (needEvict) {
                        //淘汰的时候都做一次同步
                        needSync = true;
                    }

                    boolean synced = false;
                    if (needSync) {
                        //同步
                        final Pair<Integer, String> counterTypeAndId = CounterConst.parseCounterTypeAndId(counterKey);
                        Map<String, Long> counterFields = CounterConst.buildCounterFromRedisFields(fields);
                        synced = counterPersistService.sync(counterTypeAndId.first, counterTypeAndId.second, counterFields);
                        if (synced) {
                            //LUA:更新last_sync
                            wrapper.evalInOneJedis(jedis, counterKey, COUNTER_SET_SYNC_LUA.getScript(), COUNTER_SET_SYNC_LUA.getSha1(), String.valueOf(writeVersion), String.valueOf(nowSecond));
                            syncedCount++;
                        }
                        LOGGER.debug("sync counter key:{},success:{},last sync:{}", counterKey, synced, writeVersion);
                    }

                    if (needEvict) {
                        if (!needSync || synced) {
                            //LUA:淘汰counter key,同时也会将counter_key从sync_set中删除
                            List evictResult = (List) wrapper.evalInOneJedis(jedis, counterKey, COUNTER_EVICT_LUA.getScript(), COUNTER_EVICT_LUA.getSha1(), syncSetSlotKey, String.valueOf(writeVersion));
                            int evicted = ((Number) evictResult.get(0)).intValue();
                            LOGGER.debug("try evict counter key:{},evicted:{}", counterKey, LuaScript.isTrue(evicted));
                            if (LuaScript.isTrue(evicted)) {
                                needEvictItemCount--;
                                evictedCount++;
                                batchRemoved++;
                            }
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("process counter key'" + counterKey + "' fail", e);
                }
            }
        }
        LOGGER.info("finish scan slotKey:{} in {} ms,slot length:{},evicted:{},synced:{}", syncSetSlotKey, System.currentTimeMillis() - st, originLength, evictedCount, syncedCount);
    }

    public IRedis getSyncSetRedis() {
        final String redisServiceGroup = this.syncSetCacheParameter.getRedisServiceGroup();
        try {
            return this.cacheServcie.get(redisServiceGroup);
        } catch (Exception e) {
            LOGGER.error("can't get redis for group:`" + redisServiceGroup + "` @" + this.getRedisCounterService().getName(), e);
            throw new RuntimeException(e);
        }
    }

    public boolean isStop() {
        return stop;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }

    public RedisCounterService getRedisCounterService() {
        return redisCounterService;
    }
}

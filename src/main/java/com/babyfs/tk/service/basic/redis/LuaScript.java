package com.babyfs.tk.service.basic.redis;

import com.babyfs.tk.service.basic.INameResourceService;
import com.babyfs.tk.service.biz.cache.CacheParameter;
import com.babyfs.tk.service.biz.cache.CacheUtils;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.hash.Hashing;
import com.google.common.io.Resources;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * Redis Lua Script
 */
public class LuaScript {
    private static final Logger LOGGER = LoggerFactory.getLogger(LuaScript.class);
    public static final int LUA_FALSE = 0;
    public static final int LUA_TRUE = 1;
    /**
     * 乐观锁
     */
    public static final LuaScript LOCK_OPTIMISTIC = createRedisLuaScript("lua/lock_optimistic.lua");

    private final String name;
    private final String script;
    private final String sha1;

    /**
     * @param name   名称
     * @param script 脚本内容
     */
    public LuaScript(String name, String script) {
        this.name = Preconditions.checkNotNull(name);
        this.script = Preconditions.checkNotNull(script);
        this.sha1 = Hashing.sha1().hashString(script, Charsets.UTF_8).toString();
    }

    public String getName() {
        return name;
    }

    public String getScript() {
        return script;
    }

    public String getSha1() {
        return sha1;
    }

    /**
     * @param luaPath
     * @return
     */
    public static LuaScript createRedisLuaScript(String luaPath) {
        LuaScript luaScript = new LuaScript(luaPath, loadScript(luaPath));
        LOGGER.info("load lua script:{},sha1:{}", luaScript.getName(), luaScript.getSha1());
        return luaScript;
    }

    /**
     * 加载脚本
     *
     * @param script
     * @return
     */
    public static String loadScript(String script) {
        try {
            URL url = Resources.getResource(script);
            String content = Preconditions.checkNotNull(StringUtils.trimToNull(Resources.toString(url, Charsets.UTF_8)));
            LOGGER.info("load script {},size:{}", script, content.length());
            return content;
        } catch (IOException e) {
            LOGGER.error("load lua script " + script + " fail", e);
            throw new RuntimeException(e);
        }
    }

    public static boolean isTrue(int val) {
        return val == LUA_TRUE;
    }

    public static boolean isFalse(int val) {
        return val == LUA_FALSE;
    }


    /**
     * 利用redis加乐观锁
     *
     * @param lockKey            锁key,非空
     * @param lockSeconds        锁的秒数,>0
     * @param lockCacheParameter redis组,非空
     * @param cacheService       cache service,非空
     * @return
     */
    public static boolean tryLock(String lockKey, int lockSeconds, CacheParameter lockCacheParameter, INameResourceService<IRedis> cacheService) {
        Preconditions.checkNotNull(lockKey);
        Preconditions.checkArgument(lockSeconds > 0, "lockSeconds >0");
        Preconditions.checkNotNull(lockCacheParameter);
        Preconditions.checkNotNull(cacheService);

        IRedis redis = CacheUtils.getRedisCacheClient(cacheService, lockCacheParameter.getRedisServiceGroup());
        List result = (List) redis.eval(lockCacheParameter.getCacheKey(lockKey), LOCK_OPTIMISTIC.getScript(), LOCK_OPTIMISTIC.getSha1(), String.valueOf(lockSeconds));
        Number eval = (Number) result.get(0);
        return isTrue(eval.intValue());
    }

    /**
     * 释放锁
     *
     * @param lockKey
     * @param lockCacheParameter
     * @param cacheService
     * @return
     */
    public static void unLock(String lockKey, CacheParameter lockCacheParameter, INameResourceService<IRedis> cacheService) {
        Preconditions.checkNotNull(lockKey);
        Preconditions.checkNotNull(lockCacheParameter);
        Preconditions.checkNotNull(cacheService);
        try {
            CacheUtils.delete(lockKey, lockCacheParameter, cacheService);
        } catch (Exception e) {
            //ignore it
        }
    }
}

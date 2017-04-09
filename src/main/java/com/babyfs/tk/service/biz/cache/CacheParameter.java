package com.babyfs.tk.service.biz.cache;


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * 缓存查询配置信息
 */
public class CacheParameter {

    /**
     * 失效时间
     */
    private int redisExpireSecond;
    /**
     * redis组
     */
    private String redisServiceGroup;
    /**
     * key前缀
     */
    private String redisKeyPrefix;
    /**
     * key后缀
     */
    private String redisKeySuffix;

    public CacheParameter(int redisExpireSecond, String redisServiceGroup, String redisKeyPrefix, String redisKeySuffix) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(redisServiceGroup), "redisServiceGroup is null ");
        Preconditions.checkArgument(redisKeyPrefix != null, "redisKeyPrefix is null ");
        Preconditions.checkArgument(redisKeySuffix != null, " redisKeySuffix is null ");
        this.redisExpireSecond = redisExpireSecond;
        this.redisServiceGroup = redisServiceGroup;
        this.redisKeyPrefix = redisKeyPrefix;
        this.redisKeySuffix = redisKeySuffix;
    }


    /**
     * 获取CacheKey方法
     *
     * @param id 对象主键
     * @return
     * @throws RuntimeException
     */
    public String getCacheKey(long id) {
        return this.redisKeyPrefix + id + this.redisKeySuffix;
    }

    public String getCacheKey(String id) {
        return this.redisKeyPrefix + id + this.redisKeySuffix;
    }

    public int getRedisExpireSecond() {
        return redisExpireSecond;
    }

    public String getRedisServiceGroup() {
        return redisServiceGroup;
    }

    public String getRedisKeyPrefix() {
        return redisKeyPrefix;
    }

    public String getRedisKeySuffix() {
        return redisKeySuffix;
    }

    /**
     * 重设过期时间
     *
     * @param redisExpireSecond
     * @return
     */
    public CacheParameter of(int redisExpireSecond) {
        return new CacheParameter(redisExpireSecond, this.redisServiceGroup, this.redisKeyPrefix, this.redisKeySuffix);
    }
}

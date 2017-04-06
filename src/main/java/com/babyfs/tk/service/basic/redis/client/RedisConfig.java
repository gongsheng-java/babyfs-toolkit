package com.babyfs.tk.service.basic.redis.client;

import com.babyfs.tk.service.basic.redis.Constants;

/**
 * Redis基础配置
 */
public class RedisConfig {

    /**
     * socket超时：毫秒 *
     */
    private int timeout = Constants.DEFAULT_TIMEOUT;
    /**
     * 链接池最大空闲数
     */
    private int poolMaxIdel = Constants.DEFAULT_MAX_IDEL;
    /**
     * 链接池最小空闲数
     */
    private int poolMinIdel = Constants.DEFAULT_MIN_IDEL;
    /**
     * 链接池最大活动链接数
     */
    private int poolMaxActive = Constants.DEFAULT_MAX_ACTIVE;
    /**
     * 链接池最长等待
     */
    private long poolMaxWait = Constants.DEFAULT_MAX_WAIT;

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getPoolMaxIdel() {
        return poolMaxIdel;
    }

    public void setPoolMaxIdel(int poolMaxIdel) {
        this.poolMaxIdel = poolMaxIdel;
    }

    public int getPoolMinIdel() {
        return poolMinIdel;
    }

    public void setPoolMinIdel(int poolMinIdel) {
        this.poolMinIdel = poolMinIdel;
    }

    public int getPoolMaxActive() {
        return poolMaxActive;
    }

    public void setPoolMaxActive(int poolMaxActive) {
        this.poolMaxActive = poolMaxActive;
    }

    public long getPoolMaxWait() {
        return poolMaxWait;
    }

    public void setPoolMaxWait(long poolMaxWait) {
        this.poolMaxWait = poolMaxWait;
    }
}

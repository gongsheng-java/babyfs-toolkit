package com.babyfs.tk.service.basic.redis;

/**
 * Redis服务的一些常量配置
 * <p/>
 * Created by IntelliJ IDEA.
 */
public final class Constants {

    /**
     * redis Client(socket)默认超时配置
     */
    public static final int DEFAULT_TIMEOUT = 2000;
    /**
     * timeout 配置key
     */
    public static final String CONF_KEY_TIMEOUT = "redis.timeout";

    /**
     * 链接池最大空闲数
     */
    public static final int DEFAULT_MAX_IDEL = 10;
    /**
     * 链接池最大空闲数  配置key
     */
    public static final String CONF_KEY_MAX_IDEL = "redis.pool.max.idel";

    /**
     * 链接池最小空闲数
     */
    public static final int DEFAULT_MIN_IDEL = 8;
    /**
     * 链接池最小空闲数   配置key
     */
    public static final String CONF_KEY_MIN_IDEL = "redis.pool.min.idel";

    /**
     * 链接池最大活动链接数
     */
    public static final int DEFAULT_MAX_ACTIVE = 100;
    /**
     * 链接池最大活动链接数   配置key
     */
    public static final String CONF_KEY_MAX_ACTIVE = "redis.pool.max.active";

    /**
     * 链接池最长等待
     */
    public static final long DEFAULT_MAX_WAIT = -1L;
    /**
     * 链接池最长等待 配置key
     */
    public static final String CONF_KEY_MAX_WAIT = "redis.pool.max.wait";


    private Constants() {

    }
}

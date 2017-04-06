package com.babyfs.tk.service.biz.service.counter.constants;

/**
 * 计数服务常量
 * <p/>
 */
public interface CounterConst {

    /**
     * 生成id失败时返回的无效id
     */
    public static final long INVALID_ID = -1;

    /**
     * 计数服务的Redis所属的Cache Group
     */
    public static final String CACHE_GROUP_COUNTER_SERVICE = "counter";

    /**
     * 每日计数服务的Redis所属的Cache Group
     */
    public static final String CACHE_GROUP_DAILY_COUNTER = "daily.counter";

    /**
     * 每日计数服务 缓存时间（单位为秒）--两天48小时
     */
    public static final int CACHE_TIME_DAILY_COUNTER = 60 * 60 * 24 * 2;


}

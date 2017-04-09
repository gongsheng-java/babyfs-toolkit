package com.babyfs.tk.service.biz.counter;

import java.util.Date;

/**
 * ID生成服务接口
 * <p/>
 * <p/>
 * 1.使用时需要集成基础服务Redis相关Module
 * 2.需要在redis-client配置名为{@link com.babyfs.tk.service.biz.constants.CacheConst#DEFAULT_COUNTER_GROUP}
 * <p/>
 */
public interface IDSequenceService {
    long INVALID_ID = -1;

    /**
     * 根据表名key,获得下一个增量id
     * 参数非法时，返回{@link #INVALID_ID}
     *
     * @param key 表名key
     * @return 下一个增量
     * @throws Exception 当redis工作异常时，throw {@link RuntimeException}
     */
    long getNext(String key) throws Exception;

    /**
     * 根据表名key，清空计数器
     *
     * @param key 表名key
     * @throws Exception 当redis工作异常时，throw {@link RuntimeException}
     */
    void resetCounter(String key) throws Exception;

    /**
     * 根据表名key,获得当日下一个增量id,每日零时从零开始计数
     * 参数非法时，返回{@link #INVALID_ID}
     *
     * @param key  表名key
     * @param date 日期时间
     * @return 下一个增量
     * @throws Exception 当redis工作异常时，throw {@link RuntimeException}
     */
    long getDailyNext(String key, Date date) throws Exception;
}

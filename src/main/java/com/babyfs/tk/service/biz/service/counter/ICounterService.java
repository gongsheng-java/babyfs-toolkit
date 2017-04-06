package com.babyfs.tk.service.biz.service.counter;

import com.babyfs.tk.service.biz.service.counter.constants.CounterConst;

import java.util.Date;

/**
 * 计数服务接口
 * <p/>
 * <p/>
 * 1.使用时需要集成基础服务Redis相关Module
 * 2.需要在redis-client配置名为{@link CounterConst#CACHE_GROUP_COUNTER_SERVICE}的Group
 * <p/>
 */
public interface ICounterService {

    /**
     * 根据表名key,获得下一个增量id
     * 参数非法时，返回{@link CounterConst#INVALID_ID}
     *
     * @param key 表名key
     * @return 下一个增量
     * @throws Exception 当redis工作异常时，throw {@link RuntimeException}
     */
    public long getNext(String key) throws Exception;

    /**
     * 根据表名key，清空计数器
     *
     * @param key 表名key
     * @throws Exception 当redis工作异常时，throw {@link RuntimeException}
     */
    public void resetCounter(String key) throws Exception;

    /**
     * 根据表名key,获得当日下一个增量id,每日零时从零开始计数
     * 参数非法时，返回{@link CounterConst#INVALID_ID}
     *
     * @param key  表名key
     * @param date 日期时间
     * @return 下一个增量
     * @throws Exception 当redis工作异常时，throw {@link RuntimeException}
     */
    public long getDailyNext(String key, Date date) throws Exception;
}

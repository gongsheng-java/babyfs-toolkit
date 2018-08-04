package com.babyfs.tk.service.biz.serialnum;

import com.babyfs.tk.service.biz.serialnum.enums.SerialNumType;

/**
 * 流水号服务
 * 使用时需要集成{@link com.babyfs.tk.service.biz.counter.IDSequenceService}
 */
public interface ISerialNumService {

    /**
     * 根据类型获取流水号
     * 总共22位，12位时间，2位机器号，2位类型，6位递增序列
     * @param type
     * @return
     */
    String getSerialNum(SerialNumType type);

    /**
     * snowflake算法获取流水号，强依赖时钟，机器时钟回拨时有可能会产生重复的流水号
     * 总共28位，17位时间，5位递增序列，4位机器，2位类型
     * @return
     */
    String getSFSerialNum(SerialNumType type);
}

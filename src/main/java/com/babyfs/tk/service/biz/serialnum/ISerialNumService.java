package com.babyfs.tk.service.biz.serialnum;

import com.babyfs.tk.service.biz.serialnum.enums.SerialNumType;

/**
 * 流水号服务，支持1024台机器和千万流水
 */
public interface ISerialNumService {

    /**
     * 根据类型获取流水号
     * 总共28位，16位时间，4位机器号，8位递增序列
     * @param type
     * @return
     */
    String getSerialNum(SerialNumType type);
}

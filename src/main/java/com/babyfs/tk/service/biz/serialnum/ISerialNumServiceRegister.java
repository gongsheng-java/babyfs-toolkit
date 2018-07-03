package com.babyfs.tk.service.biz.serialnum;

import com.babyfs.tk.commons.service.ILifeService;

/**
 * 交易流水服务注册器
 */
public interface ISerialNumServiceRegister extends ILifeService {
    /**
     * 获取机器编号
     * @return
     */
    int getSerialNum();
}

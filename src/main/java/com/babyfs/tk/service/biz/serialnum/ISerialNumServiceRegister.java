package com.babyfs.tk.service.biz.serialnum;

import com.babyfs.tk.commons.service.ILifeService;

/**
 * 交易流水服务注册器
 * 1、机器根据IP注册到zk
 * 2、使用zk的永久递增序列获取机器编号
 */
public interface ISerialNumServiceRegister extends ILifeService {
    /**
     * 获取机器编号
     * @return
     */
    int getSerialNum();
}

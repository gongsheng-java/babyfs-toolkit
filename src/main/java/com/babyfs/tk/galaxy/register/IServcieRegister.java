package com.babyfs.tk.galaxy.register;

import com.babyfs.tk.commons.service.ILifeService;

import java.util.List;

/**
 * 服务注册
 */
public interface IServcieRegister extends ILifeService {
    /**
     * 增加服务
     *
     * @param serviceNames
     */
    void addServices(List<String> serviceNames);

    /**
     * 删除服务
     *
     * @param serviceNames
     */
    void removeServices(List<String> serviceNames);

    /**
     * 更新
     */
    void updateRegister();
}

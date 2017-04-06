package com.babyfs.tk.commons.name;

import com.babyfs.tk.commons.event.IEventListener;

import javax.annotation.Nonnull;

/**
 * 服务提供者的注册接口
 */
public interface INameServiceRegister {
    /**
     * 增加服务信息
     *
     * @param service
     */
    void addService(@Nonnull String service);

    /**
     * 删除服务信息
     *
     * @param service
     */
    void removeService(@Nonnull String service);

    /**
     * 增加关注的事件
     *
     * @param listener
     */
    void addListener(@Nonnull IEventListener<NSRegisterEvent> listener);

    /**
     * 向Zookeeper注册服务信息
     *
     * @return 是否注册成功
     */
    boolean register();

    /**
     * 取消注册
     *
     * @return
     */
    boolean unRegister();
}

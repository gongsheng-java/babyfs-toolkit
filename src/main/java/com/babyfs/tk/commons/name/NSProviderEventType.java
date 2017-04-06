package com.babyfs.tk.commons.name;

/**
 * 命名服务提供者的事件类型
 */
public enum NSProviderEventType {
    /**
     * 初始化Server
     */
    INIT,
    /**
     * 增加一个Server
     */
    ADD_SERVER,
    /**
     * 删除一个Server
     */
    DELETE_SERVER,
    /**
     * 请求重新初始化,当发生严重的错误时使用该类型的事件强制初始化数据
     */
    REINIT;
}

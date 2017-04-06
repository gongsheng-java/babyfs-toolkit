package com.babyfs.tk.commons.service;

import com.google.common.util.concurrent.Service;

/**
 * 带有启动和停止的生命周期的service基本接口
 */
public interface ILifeService extends Service {
    /**
     * 取得服务的名称
     *
     * @return
     */
    String getName();
}

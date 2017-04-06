package com.babyfs.tk.commons.config;

import com.google.common.collect.ImmutableMap;

/**
 * 全局服务
 */
public interface IGlobalService {
    /**
     * 取得全局配置
     *
     * @return
     */
    IConfigService getGlobalConfigService();
}

package com.babyfs.tk.dubbo.guice;

import com.alibaba.dubbo.config.ServiceConfig;

/**
 * {@link com.alibaba.dubbo.config.ServiceConfig#ref}的注入接口
 *
 * @see {@link ServiceConfigInjectHelperGenerator}
 */
public interface IServiceConfigInjectHelper {
    /**
     * @param config
     */
    public void setServiceConfig(ServiceConfig config);
}

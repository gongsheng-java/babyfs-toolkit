package com.babyfs.tk.galaxy.register;

/**
 * 服务发现配置接口
 */
public interface IRpcConfigService {

    //本服务启动端口
    int getPort();

    //本服务的应用名称
    String getAppName();
}

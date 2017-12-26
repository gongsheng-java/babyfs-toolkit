package com.babyfs.tk.galaxy.register;

/**
 * 服务发现配置接口
 */
public interface DiscoveryProperties {


    public boolean isEnabled();

    public String getDiscoveryPrefix();

    public boolean isPreferIpAddress();

    public int getTtl();

    public int getHeartbeatInterval();

    public String getRegisterUrl();


    public int getConnectTimeOut();


    public int getSessionTimeOut();


    public String getPort();


    public String getAppName();


    public String getHostname();

}

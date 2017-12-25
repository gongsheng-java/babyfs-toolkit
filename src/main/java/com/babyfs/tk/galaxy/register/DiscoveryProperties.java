package com.babyfs.tk.galaxy.register;

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

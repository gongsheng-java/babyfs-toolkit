package com.babyfs.tk.galaxy.register;

/**
 * 服务发现配置接口
 */
public interface IDiscoveryProperties {

     //服务发现client监听的注册节点的前缀
     String getDiscoveryPrefix();
     //注册中心的地址
     String getRegisterUrl();
     //zk连接超时时间
     int getConnectTimeOut();
     //zk session超时时间
     int getSessionTimeOut();
     //本服务启动端口
     String getPort();
     //本服务的应用名称
     String getAppName();
     //本服务的ip地址
     String getHostname();

     String getUrlPrefix();

}

package com.babyfs.tk.galaxy.register;

/**
 * 服务实例pojo
 */
public class ServiceInstance {



    //应用名称
    private String appName;
    //应用启动的端口号
    private String port;
    //应用的ip地址
    private String host;


    public ServiceInstance(String appName, String host, String port) {
        this.appName = appName;
        this.host = host;
        this.port = port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "ServiceInstance{" +
                "appName='" + appName + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                '}';
    }
}

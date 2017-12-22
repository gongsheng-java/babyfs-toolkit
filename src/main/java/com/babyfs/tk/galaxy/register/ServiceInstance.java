package com.babyfs.tk.galaxy.register;

public class ServiceInstance {


    private String appName;

    private String  port;

    private String host;

    public ServiceInstance(String appName, String host, String port) {
        this.appName = appName;
        this.host = host;
        this.port = port;
    }

    @Override
    public String toString() {
        return "ServiceInstance{" +
                "appName='" + appName + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                '}';
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

    public void setPort(String port) {
        this.port = port;
    }



}

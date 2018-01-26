package com.babyfs.tk.galaxy.register;


/**
 * 服务实例pojo
 */
public class ServiceInstance {

    //应用名称
    private String appName;
    //应用启动的端口号
    private int port;
    //应用的ip地址
    private String host;

    public ServiceInstance(String appName, String host, int port) {
        this.appName = appName;
        this.host = host;
        this.port = port;
    }

    public void setPort(int port) {
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

    public int getPort() {
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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ServiceInstance) {
            ServiceInstance other = (ServiceInstance) obj;
            return other.getPort() == (((ServiceInstance) obj).getPort()) && other.getHost().equals(((ServiceInstance) obj).host)
                    && other.getAppName().equals(((ServiceInstance) obj).getAppName());
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + host.hashCode();
        result = 31 * result + appName.hashCode();
        result = 31 * result + port;
        return result;
    }
}

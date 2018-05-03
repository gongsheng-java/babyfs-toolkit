package com.babyfs.tk.galaxy.register;

import java.util.Objects;

/**
 * 服务实例
 */
public class ServiceInstance {
    /**
     * 应用的ip地址
     */
    private String host;
    /**
     * 应用启动的端口号
     */
    private int port;

    public ServiceInstance(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void setPort(int port) {
        this.port = port;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceInstance that = (ServiceInstance) o;
        return port == that.port &&
                Objects.equals(host, that.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port);
    }

    @Override
    public String toString() {
        return "ServiceInstance{" +
                "host='" + host + '\'' +
                ", port=" + port +
                '}';
    }
}

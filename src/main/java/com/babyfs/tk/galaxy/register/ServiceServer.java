package com.babyfs.tk.galaxy.register;

import com.google.common.collect.Sets;

import java.util.Objects;
import java.util.Set;

/**
 * 服务的Server
 */
public class ServiceServer {
    /**
     * 服务注册的token
     */
    private String token;
    /**
     * 应用的ip地址
     */
    private String host;
    /**
     * 应用启动的端口号
     */
    private int port;

    private Set<String> servcies = Sets.newHashSet();

    public ServiceServer() {
    }

    public ServiceServer(String token, String host, int port) {
        this.token = token;
        this.host = host;
        this.port = port;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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

    public Set<String> getServcies() {
        return servcies;
    }

    public void setServcies(Set<String> servcies) {
        this.servcies = servcies;
    }

    public void addService(Set<String> serviceNames) {
        if (this.servcies == null) {
            this.servcies = Sets.newHashSet();
        }
        this.servcies.addAll(serviceNames);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceServer that = (ServiceServer) o;
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

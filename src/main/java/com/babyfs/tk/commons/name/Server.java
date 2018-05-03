package com.babyfs.tk.commons.name;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * 服务实例的信息
 */
public class Server {
    /**
     * 服务器监听的IP
     */
    private final String ip;
    /**
     * 服务器监听的端口
     */
    private final int port;

    /**
     * 由该服务器提供的服务名称
     */
    private final Set<String> services = Sets.newHashSet();

    /**
     * @param ip
     * @param port
     */
    public Server(String ip, int port) {
        checkArgument(!Strings.isNullOrEmpty(ip), "The server ip must not be null or empty.");
        checkArgument(port > 0, "The server port must be > 0.");
        this.ip = ip;
        this.port = port;
    }

    public synchronized void addService(@Nonnull String serviceName) {
        checkArgument(!Strings.isNullOrEmpty(serviceName), "The service name to be added must not be null or empty.");
        this.services.add(serviceName);
    }

    public synchronized void addService(@Nonnull Collection<String> services) {
        checkArgument(services != null, "The services must not be null.");
        this.services.addAll(services);
    }

    public synchronized boolean removeService(@Nonnull String serviceName) {
        checkArgument(!Strings.isNullOrEmpty(serviceName), "The service name must to be removed not be null or empty.");
        return this.services.remove(serviceName);
    }

    public Set<String> getServices() {
        return Collections.unmodifiableSet(services);
    }


    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Server server = (Server) o;
        return port == server.port &&
                Objects.equals(ip, server.ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, port);
    }

    @Override
    public String toString() {
        return "Server{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                '}';
    }
}

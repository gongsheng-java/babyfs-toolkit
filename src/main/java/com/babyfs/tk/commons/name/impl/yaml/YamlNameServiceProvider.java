package com.babyfs.tk.commons.name.impl.yaml;

import com.babyfs.tk.commons.Constants;
import com.babyfs.tk.commons.event.IEventListener;
import com.babyfs.tk.commons.name.NSProviderEventType;
import com.babyfs.tk.commons.name.Server;
import com.babyfs.tk.commons.name.INameServiceProvider;
import com.babyfs.tk.commons.name.NSProviderEvent;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.io.Closeables;
import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import static com.google.common.base.Preconditions.*;

/**
 * 基于YAML配置文件实现的命名服务提供者
 */
public class YamlNameServiceProvider implements INameServiceProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(YamlNameServiceProvider.class);

    /**
     * YAML文件的配置
     */
    private String config;
    /**
     * YAML文件的编码
     */
    private String configEncoding;

    /**
     * Server事件监听器集合
     */
    protected ConcurrentMap<IEventListener<NSProviderEvent>, IEventListener<NSProviderEvent>> listeners = Maps.newConcurrentMap();

    private volatile List<Server> servers;

    /**
     * @param config
     */
    public YamlNameServiceProvider(@Nonnull String config) {
        this(config, Constants.UTF_8);
    }

    /**
     * @param config         YAML配置文件的路径,该文件应该可以在classpath中找到
     * @param configEncoding YAML配置文件的编码
     */
    public YamlNameServiceProvider(@Nonnull String config, @Nonnull String configEncoding) {
        checkArgument(!Strings.isNullOrEmpty(config), "The config must be set");
        checkArgument(!Strings.isNullOrEmpty(configEncoding), "The configEncoding must be set.");
        this.config = config;
        this.configEncoding = configEncoding;
        this.servers = Collections.unmodifiableList(this.loadAllServices());
    }

    @Override
    public void reload() {
        this.servers = this.loadAllServices();
        List<Server> allServiceServers = this.servers;
        NSProviderEvent event = new NSProviderEvent(NSProviderEventType.INIT, allServiceServers);
        for (IEventListener<NSProviderEvent> listener : this.listeners.values()) {
            try {
                listener.onEvent(event);
            } catch (Exception e) {
                LOGGER.error("Faile to trigger the listener [" + listener + "]", e);
            }
        }
    }

    @Override
    public <T> T init(Function<List<Server>, T> function) {
        return function.apply(this.servers);
    }


    /**
     * 从配置文件中加载服务信息
     *
     * @return
     */
    private List<Server> loadAllServices() {
        List<Server> serviceServers = new ArrayList<Server>();
        URL resource = Resources.getResource(config);
        InputStream in = null;
        try {
            in = resource.openStream();
            Reader reader = new InputStreamReader(in, this.configEncoding);
            Yaml yaml = new Yaml();
            Object nameServices = yaml.load(reader);
            checkNotNull(nameServices, "Except a java.util.Map object,but it's null");
            checkState(nameServices instanceof Map, "Except a java.util.Map object,but it's %s", nameServices.getClass());
            Map nsMap = (Map) nameServices;
            Object tServers = nsMap.get("servers");
            checkState(tServers != null, "The serverList must not be null");
            checkState(tServers instanceof List, "The servers must be an sequence[java.util.List],but it's %s ", tServers == null ? null : tServers.getClass());
            List serversList = (List) tServers;
            checkNotNull(serversList, "The serverList must not be null");
            checkState(!serversList.isEmpty(), "The servers list must not be empty");
            for (Object server : serversList) {
                if (server == null) {
                    throw new IllegalStateException("The server must be a java.util.Map,but it's null");
                }
                checkState(Map.class.isAssignableFrom(server.getClass()), "The server must be a java.util.Map,but it's " + server.getClass());
                Map serverMap = (Map) server;
                Object svr = serverMap.get("svr");
                Map svrMap = (Map) svr;
                String id = (String) svrMap.get("id");
                String ip = (String) svrMap.get("ip");
                int port = (Integer) svrMap.get("port");
                checkState(!Strings.isNullOrEmpty(id), "The id must not be empty.");
                checkState(!Strings.isNullOrEmpty(ip), "The ip must not be empty.");
                checkState(port > 0, "The port must be > 0");

                Server serverInstance = new Server(id, ip, port);
                Object services = serverMap.get("services");
                checkState(services instanceof List, "The services must be a java.util.List,but it's %s.", services == null ? null : services.getClass());
                List servicesList = (List) services;
                for (Object service : servicesList) {
                    checkState(service instanceof String, "port must be > 0");
                    serverInstance.addService((String) service);
                }
                serviceServers.add(serverInstance);
            }
        } catch (IOException e) {
            throw new RuntimeException("Can't open the config from " + resource, e);
        } finally {
            Closeables.closeQuietly(in);
        }
        return Collections.unmodifiableList(serviceServers);
    }

    @Override
    public void addListener(@Nonnull IEventListener<NSProviderEvent> eventListener) {
        checkArgument(eventListener != null, "eventListener");
        this.listeners.put(eventListener, eventListener);
    }
}

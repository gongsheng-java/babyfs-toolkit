package com.babyfs.tk.commons.name;

import com.babyfs.tk.commons.collect.CopyOnWriteArray;
import com.babyfs.tk.commons.event.IEventListener;
import com.babyfs.tk.commons.thread.NamedThreadFactory;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * 服务信息注册
 */
public class ServiceRegistry implements IEventListener<NSProviderEvent>, INameService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRegistry.class);
    /**
     * 定时任务维护的线程池
     */
    private static final ExecutorService EXECUTORS = Executors.newSingleThreadExecutor(new NamedThreadFactory("ServiceRegistry"));
    /**
     * 服务信息注册
     */
    private volatile RegistryMaps regMaps = new RegistryMaps();
    /**
     * 命名服务提供者
     */
    private volatile INameServiceProvider nameServiceProvider;
    /**
     * 记录查询服务的次数,用于实现一个简单的负载均衡策略
     */
    private volatile int seq = 0;

    /**
     * 尝试重新获取注册信息的标志
     */
    private final AtomicBoolean reTry = new AtomicBoolean(false);
    /**
     * 是否重新初始化
     */
    private final AtomicBoolean reInit = new AtomicBoolean(false);
    /**
     * 初始化命名服务的回调接口
     */
    private final Function<List<Server>, Void> initFunction = new Function<List<Server>, Void>() {
        @Override
        public Void apply(@Nullable List<Server> input) {
            if (input == null) {
                LOGGER.warn("Init fail,input is null,try to reinit.");
                reInit.compareAndSet(false, true);
            } else {
                onEvent(new NSProviderEvent(NSProviderEventType.INIT, input));
            }
            return null;
        }
    };
    /**
     * 重新初始化的任务
     */
    private final Runnable reInitTask = new Runnable() {
        @Override
        public void run() {
            try {
                nameServiceProvider.reload();
            } finally {
                reTry.set(false);
            }
        }
    };


    public ServiceRegistry() {
        LOGGER.warn("No INameServiceProvider");
    }

    /**
     * @param nameServiceProvider
     */
    public ServiceRegistry(@Nonnull INameServiceProvider nameServiceProvider) {
        LOGGER.info("Use INameServiceProvider:" + nameServiceProvider);
        this.nameServiceProvider = nameServiceProvider;
        this.nameServiceProvider.addListener(this);
        this.nameServiceProvider.init(initFunction);
    }

    /**
     * 添加一个Server,并将该Server的服务接口加入到注册机制当中
     *
     * @param server
     */
    public synchronized void addServer(@Nonnull Server server) {
        checkArgument(server != null, "The server must not be null.");
        final RegistryMaps curMaps = this.regMaps;
        addServer(server, curMaps);
    }


    /**
     * 删除一个server,同时将其从service注册中中删除
     *
     * @param server
     */
    public synchronized void removeServer(@Nonnull Server server) {
        checkArgument(server != null, "The server must not be null.");
        LOGGER.warn("Remove the server {}", server);
        final RegistryMaps curMaps = this.regMaps;
        Server realServer = curMaps.servers.get(server);
        if (realServer == null) {
            LOGGER.warn("Can't find the server with %s", server);
            return;
        }
        Set<String> services = realServer.getServices();
        for (String service : services) {
            final ServiceServerEntry serverMap = curMaps.serviceToServer.get(service);
            if (serverMap == null) {
                LOGGER.warn("Can't find the server set of the service {}.", service);
                continue;
            }
            serverMap.removeServer(server);
            LOGGER.info("Remove the server {} from the server set of the service {}.", server, service);
        }
    }


    /**
     * 处理被触发的事件
     *
     * @param event
     */
    @Override
    public synchronized void onEvent(@Nonnull NSProviderEvent event) {
        checkArgument(event != null, "event");
        NSProviderEventType type = event.getType();
        List<Server> value = event.getValue();
        if (value == null) {
            LOGGER.warn("Null value for event " + type + ",ignore it.");
            return;
        }
        LOGGER.info("Receive event:" + type + ",value:" + value);
        switch (type) {
            case INIT:
                //重新初始化
                RegistryMaps newMaps = new RegistryMaps();
                for (Server server : value) {
                    this.addServer(server, newMaps);
                }
                this.regMaps = newMaps;
                LOGGER.info("Set the regMaps to newMaps.");
                this.reInit.set(false);
                break;
            case ADD_SERVER:
                //增加server
                for (Server server : value) {
                    this.addServer(server);
                }
                break;
            case DELETE_SERVER:
                //删除server
                for (Server server : value) {
                    this.removeServer(server);
                }
                break;
            case REINIT:
                this.reInit.compareAndSet(false, true);
                break;
            default:
                LOGGER.warn("Unknown type:" + type);
                break;
        }
    }

    /**
     * 根据服务名查询一个server
     *
     * @param serviceName
     */
    @Override
    public Server findServerByServiceName(String serviceName) {
        final RegistryMaps curMaps = this.regMaps;
        final ServiceServerEntry entry = curMaps.serviceToServer.get(serviceName);
        if (entry == null) {
            LOGGER.warn("Can't find the server for the servie {}", serviceName);
            checkNeedReInit();
            return null;
        }
        checkNeedReInit();
        //按次序轮流选择Server,TODO 更好的选择策略需要被考虑
        Object[] objects = entry.serversArray.getArray();
        if (objects.length == 0) {
            return null;
        }
        int index = seq++;
        if (index < 0) {
            index = 0;
            seq = 0;
        }
        index = index % objects.length;
        return (Server) objects[index];
    }

    @Override
    public Server findServerByServerId(String serviceName, String serverId) {
        final RegistryMaps curMaps = this.regMaps;
        final ServiceServerEntry entry = curMaps.serviceToServer.get(serviceName);
        if (entry == null) {
            LOGGER.warn("Can't find the server for the servie:{} serverId:{}", serviceName, serverId);
            checkNeedReInit();
            return null;
        }
        checkNeedReInit();
        LOGGER.debug("Find target server, serverId:{} serviceName:{}.", serverId, serviceName);
        Server server = entry.getServerById(serverId);
        if (server != null) {
            LOGGER.debug("Found server {} for servreId:{}.", server, serverId);
            return server;
        } else {
            LOGGER.warn("Can't find the server for serverId:{},serviceName:{}", serverId, serviceName);
        }
        return null;
    }


    /**
     * 向指定的{@link RegistryMaps} 中添加一个server
     *
     * @param server
     * @param maps
     */
    private void addServer(Server server, RegistryMaps maps) {
        Server preServer = maps.getServers().put(server, server);
        if (preServer != null) {
            /*
             * 已经存在一个相同的的server,则先删除所有仅包含在preServer中定义的service与server之间的对应关系
             */
            LOGGER.info("Pre server {} already existed", preServer);
            Set<String> preServerServices = preServer.getServices();
            Set<String> newServerServices = server.getServices();
            Sets.SetView<String> difference = Sets.difference(preServerServices, newServerServices);
            LOGGER.info("Will remove {} server for difference service", difference.size());
            for (String removeService : difference) {
                ServiceServerEntry service2server = maps.serviceToServer.get(removeService);
                Preconditions.checkState(service2server != null, "The server set of the servcie %s is null.", removeService);
                service2server.removeServer(preServer);
                LOGGER.info("Remove the server {}s from the server set of the service {} .", preServer, removeService);
            }
        } else {
            LOGGER.info("Add new server {}", server);
        }
        //添加service到server的对应关系
        Set<String> services = server.getServices();
        for (String service : services) {
            ServiceServerEntry entry = maps.serviceToServer.get(service);
            if (entry == null) {
                entry = new ServiceServerEntry();
                maps.serviceToServer.put(service, entry);
            }
            entry.addServer(server);
            LOGGER.info("Add {} to the server set of the service {} .", server, service);
        }
    }

    /**
     * 检测是否需要重新初始化
     */
    private void checkNeedReInit() {
        if (this.reInit.get()) {
            //当发现一个server都没有的时候,则认为初始化失败了,创建一个任务异步重新初始化服务
            if (reTry.compareAndSet(false, true)) {
                LOGGER.warn("ReInit");
                EXECUTORS.submit(reInitTask);
                LOGGER.warn("The reTry task has been submmited.");
            }
        }
    }


    /**
     * Service的服务器条目
     */
    private static final class ServiceServerEntry {
        private final ConcurrentMap<String, Server> serversMap = Maps.newConcurrentMap();
        private final CopyOnWriteArray serversArray = new CopyOnWriteArray();

        private ServiceServerEntry() {
        }

        public synchronized boolean addServer(@Nonnull Server server) {
            checkArgument(server != null, "server");
            boolean success = serversMap.putIfAbsent(server.getId(), server) == null;
            if (success) {
                serversArray.add(server);
            }
            return success;
        }

        public synchronized boolean removeServer(@Nonnull Server server) {
            checkArgument(server != null, "server");
            boolean success = this.serversMap.remove(server.getId()) != null;
            this.serversArray.remove(server);
            return success;
        }

        /**
         * @param id
         * @return
         */
        public Server getServerById(String id) {
            return this.serversMap.get(id);
        }
    }

    /**
     * 服务信息注册的Maps
     */
    private static class RegistryMaps {
        /**
         * 服务名称与服务器实例的对应关系
         * key:service name
         * value:server instance
         */
        private final ConcurrentMap<String, ServiceServerEntry> serviceToServer = Maps.newConcurrentMap();
        /**
         * 所有提供服务的实例
         */
        private final ConcurrentMap<Server, Server> servers = Maps.newConcurrentMap();

        RegistryMaps() {

        }


        public ConcurrentMap<String, ServiceServerEntry> getServiceToServer() {
            return serviceToServer;
        }

        public ConcurrentMap<Server, Server> getServers() {
            return servers;
        }
    }
}

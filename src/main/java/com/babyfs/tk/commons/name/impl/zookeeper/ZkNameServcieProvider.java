package com.babyfs.tk.commons.name.impl.zookeeper;

import com.alibaba.fastjson.JSON;
import com.babyfs.tk.commons.codec.ICodec;
import com.babyfs.tk.commons.event.EventUtil;
import com.babyfs.tk.commons.event.IEventListener;
import com.babyfs.tk.commons.name.NSProviderEventType;
import com.babyfs.tk.commons.name.Server;
import com.babyfs.tk.commons.utils.ListUtil;
import com.babyfs.tk.commons.zookeeper.ZkClient;
import com.babyfs.tk.commons.name.INameServiceProvider;
import com.babyfs.tk.commons.name.NSProviderEvent;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * 基于ZooKeeper实现的命名提供者实现,服务定义在Zookeeper上的结构如下:
 * <p/>
 * <pre>
 *     +---naming_service  +
 *                         |---group1_root +    #第一组服务的根节点,对于服务的分组
 *                         |               |---ip:port +    #服务器节点,所有的服务器节点名称格式应当是svr_xxxx,而svr_xxx作为该服务器的惟一节点
 *                                                        #data:byte[] JSON格式的数据,定义参见{@link Server}
 *                         |               |
 *                         |               |---ip:port +    #服务器节点,data:byte[] JSON格式的数据,定义参见{@link Server}
 *                         |
 *                         |---group2_root +    #第二组服务定义的根节点
 *                                         |---ip:port +    #服务器节点,data:byte[]
 *                                         |
 *                                         |---ip:port+    #服务器节点,data:byte[]
 *
 *
 *
 * </pre>
 */
public class ZkNameServcieProvider implements INameServiceProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZkNameServcieProvider.class);
    /**
     * Zookeeper的客户端
     */
    private final ZkClient zkClient;
    /**
     * 服务定义的根目录,用于查找服务定义的根节点
     */
    private final String serviceRootPath;
    /**
     * Zookeeper时间监听器
     */
    private final Watcher watcher = new ServiceWathcer();

    /**
     * 当前存在的server列表
     */
    private final Set<String> servers = Sets.newHashSet();

    /**
     * Server事件监听器集合
     */
    protected ConcurrentMap<IEventListener<NSProviderEvent>, IEventListener<NSProviderEvent>> listeners = Maps.newConcurrentMap();


    /**
     * @param zkClient
     * @param serviceRootPath
     */
    public ZkNameServcieProvider(@Nonnull ZkClient zkClient, @Nonnull String serviceRootPath) {
        Preconditions.checkArgument(zkClient != null, "zkClient");
        Preconditions.checkArgument(serviceRootPath != null && serviceRootPath.startsWith("/"), "The serviceRootPath must starts with / .");
        String rootPath = serviceRootPath;
        if (serviceRootPath.endsWith("/")) {
            rootPath = serviceRootPath.substring(0, serviceRootPath.length() - 1);
        }
        this.serviceRootPath = rootPath.trim();
        this.zkClient = zkClient;
        this.zkClient.register(this.watcher);
        this.zkClient.registerExpirationHandler(new Runnable() {
            @Override
            public void run() {
                LOGGER.warn("Zookeeper has exipred,reload all servevics");
                reload();
            }
        });
    }


    @Override
    public synchronized void reload() {
        LOGGER.info("Reload the services from " + this + " using init.");
        this.servers.clear();
        init(new Function<List<Server>, Void>() {
            @Override
            public Void apply(@Nullable List<Server> input) {
                NSProviderEvent event = new NSProviderEvent(NSProviderEventType.INIT, input);
                triggerEvent(event);
                return null;
            }
        });
    }

    @Override
    public synchronized <T> T init(Function<List<Server>, T> function) {
        LOGGER.info("Init the services from " + this.zkClient.getZkServers() + ":" + this.serviceRootPath);
        List<Server> list = this.buildServersFromRoot(false);
        return function.apply(list);
    }

    @Override
    public void addListener(@Nonnull IEventListener<NSProviderEvent> eventListener) {
        checkArgument(eventListener != null, "eventListener");
        this.listeners.put(eventListener, eventListener);
    }


    /**
     * @param zooKeeper
     * @param serverPath
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    private synchronized Server getServerFromNode(ZooKeeper zooKeeper, String serverPath) throws KeeperException, InterruptedException {
        byte[] data = zooKeeper.getData(serverPath, true, null);
        //增加对server节点数据的监控
        if (data == null || data.length == 0) {
            LOGGER.warn("No data at node " + serverPath);
            return null;
        }

        return JSON.parseObject(data, Server.class);
    }

    /**
     * 从服务的根节点建立服务信息
     *
     * @param notifListener
     * @return null 构建失败
     */
    private synchronized List<Server> buildServersFromRoot(boolean notifListener) {
        LOGGER.info("Build all services from root " + this.toString());
        try {
            ZooKeeper zooKeeper = this.zkClient.get(ZkConstants.CONNECTION_TIMEOUT);
            //增加对该root节点的监控
            if (zooKeeper.exists(this.serviceRootPath, true) == null) {
                this.servers.clear();
                LOGGER.error("The root path " + this.serviceRootPath + " doesn't exist.");
                if (notifListener) {
                    NSProviderEvent event = new NSProviderEvent(NSProviderEventType.INIT, Collections.<Server>emptyList());
                    triggerEvent(event);
                }
                return Collections.emptyList();
            }

            //增加对root下字节点的监控
            List<String> serverChildren = ListUtil.transform(zooKeeper.getChildren(this.serviceRootPath, true), new Function<String, String>() {
                @Override
                public String apply(@Nullable String input) {
                    return serviceRootPath + "/" + input;
                }
            });

            if (serverChildren.isEmpty()) {
                LOGGER.warn("No servers from " + this.toString());
                this.servers.clear();
                if (notifListener) {
                    NSProviderEvent event = new NSProviderEvent(NSProviderEventType.INIT, Collections.<Server>emptyList());
                    triggerEvent(event);
                }
                return Collections.emptyList();
            }

            Set[] difference = checkDifference(serverChildren);
            Set<String> added = difference[0];
            Set<String> removed = difference[1];
            List<Server> result = new ArrayList<Server>(10);

            //处理新增加的server
            for (String ns : added) {
                Server server = this.getServerFromNode(zooKeeper, ns);
                if (server == null) {
                    continue;
                }
                LOGGER.info("Build for " + ns + ",add server " + server);
                if (notifListener) {
                    NSProviderEvent event = new NSProviderEvent(NSProviderEventType.ADD_SERVER, Lists.newArrayList(server));
                    triggerEvent(event);
                }
                result.add(server);
                this.servers.add(ns);
            }

            //处理删除的server
            for (String rs : removed) {
                this.servers.remove(rs);
                LOGGER.info("Build for root " + rs + ",remove server " + rs);
                if (notifListener) {
                    NSProviderEvent event = new NSProviderEvent(NSProviderEventType.DELETE_SERVER, Lists.newArrayList(new Server(rs, 1)));
                    triggerEvent(event);
                }
            }
            return result;
        } catch (Exception e) {
            onException(e);
        }
        return null;
    }

    /**
     * 当服务节点的数据变化时通知节点变化的内容
     *
     * @param serverPath
     * @param notifListener
     * @return
     */
    private synchronized List<Server> onServerDataChanged(String serverPath, boolean notifListener) {
        try {
            List<Server> result = new ArrayList<Server>(1);
            ZooKeeper zooKeeper = zkClient.get(ZkConstants.CONNECTION_TIMEOUT);
            //处理新增加的server
            Server server = this.getServerFromNode(zooKeeper, serverPath);
            if (server == null) {
                return null;
            }
            LOGGER.info("Server data changed " + server + ",add server");
            if (notifListener) {
                NSProviderEvent event = new NSProviderEvent(NSProviderEventType.ADD_SERVER, Lists.newArrayList(server));
                triggerEvent(event);
            }
            result.add(server);
            this.servers.add(serverPath);
            return result;
        } catch (Exception e) {
            onException(e);
        }
        return null;
    }

    /**
     * 计算Server节点的变化
     *
     * @param fullServers
     * @return [0], 新增加的节点;[1],删除的节点
     */
    private synchronized Set[] checkDifference(Collection<String> fullServers) {
        Set<String> newAddServers = Sets.newHashSet();
        Set<String> newRemoeServers = Sets.newHashSet();
        //计算新增加的
        for (String fs : fullServers) {
            if (!servers.contains(fs)) {
                newAddServers.add(fs);
            }
        }
        for (String es : servers) {
            if (!fullServers.contains(es)) {
                newRemoeServers.add(es);
            }
        }
        return new Set[]{newAddServers, newRemoeServers};
    }

    private class ServiceWathcer implements Watcher {
        @Override
        public void process(WatchedEvent event) {
            LOGGER.info("Event:" + event);
            if (event.getType() == Event.EventType.None) {
                return;
            }
            final String path = event.getPath();
            if (Strings.isNullOrEmpty(path)) {
                return;
            }

            if (!path.startsWith(serviceRootPath)) {
                LOGGER.warn("Not from serviceRootPath:" + serviceRootPath + ",skip it.");
                return;
            }

            try {
                final ZooKeeper zooKeeper = zkClient.get(ZkConstants.CONNECTION_TIMEOUT);
                if (path.equals(serviceRootPath)) {
                    //根节点的变化
                    switch (event.getType()) {
                        case NodeCreated:
                            //根节点重新建立,尝试重新获取服务器列表
                            zooKeeper.exists(path, true);
                            reload();
                            break;
                        case NodeDeleted:
                            //根节点被删除了,监听根节点的存在状态,这里是否通知上层删除所有的服务?
                            zooKeeper.exists(path, true);
                            break;
                        case NodeChildrenChanged:
                            //子节点变化了
                            buildServersFromRoot(true);
                            break;
                        case NodeDataChanged:
                            //忽略
                            break;
                        default:
                            break;
                    }
                    return;
                } else if (path.startsWith(serviceRootPath)) {
                    switch (event.getType()) {
                        case NodeCreated:
                        case NodeDeleted:
                        case NodeChildrenChanged:
                            break;
                        case NodeDataChanged:
                            onServerDataChanged(path, true);
                            break;
                        default:
                            break;
                    }

                }
            } catch (Exception e) {
                onException(e);
            }
        }

    }

    /**
     * @param e
     */
    private void onException(Exception e) {
        LOGGER.error("Fatal exception,trigger " + NSProviderEventType.REINIT, e);
        triggerReInitEvent();
    }

    private void triggerReInitEvent() {
        NSProviderEvent event = new NSProviderEvent(NSProviderEventType.REINIT, null);
        triggerEvent(event);
    }

    private void triggerEvent(NSProviderEvent event) {
        LOGGER.info("Trigger event:" + event.getType());
        EventUtil.triggerEvent(this.listeners.values(), event);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ZkNameServcieProvider");
        sb.append("{zkServers='").append(zkClient.getZkServers()).append('\'');
        sb.append(", serviceRootPath='").append(serviceRootPath).append('\'');
        sb.append('}');
        return sb.toString();
    }

}

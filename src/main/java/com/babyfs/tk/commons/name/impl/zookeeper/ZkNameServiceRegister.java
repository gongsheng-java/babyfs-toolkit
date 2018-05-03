package com.babyfs.tk.commons.name.impl.zookeeper;

import com.babyfs.tk.commons.codec.ICodec;
import com.babyfs.tk.commons.event.IEventListener;
import com.babyfs.tk.commons.name.NSRegisterEventType;
import com.babyfs.tk.commons.event.EventUtil;
import com.babyfs.tk.commons.name.INameServiceRegister;
import com.babyfs.tk.commons.name.NSRegisterEvent;
import com.babyfs.tk.commons.name.model.gen.NamingServices;
import com.babyfs.tk.commons.zookeeper.ZkClient;
import com.babyfs.tk.commons.zookeeper.ZkUtils;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

import static org.apache.zookeeper.CreateMode.EPHEMERAL;

/**
 * 向Zookeeper中注册服务信息
 */
public class ZkNameServiceRegister implements INameServiceRegister {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZkNameServiceRegister.class);

    private final ZkClient zkClient;
    private final String serverId;
    private final String ip;
    private final int port;
    private final String serviceRootPath;
    private final String nodePath;
    private final Set<String> services;
    private final String registerToken;

    /**
     * server节点数据的编码解码器
     */
    private final ICodec codec;
    /**
     *
     */
    private final RegisterWatcher watcher;

    private boolean start = true;

    /**
     * 事件监听器集合
     */
    protected ConcurrentMap<IEventListener<NSRegisterEvent>, IEventListener<NSRegisterEvent>> listeners = Maps.newConcurrentMap();

    /**
     * @param zkClient
     * @param serverId
     * @param ip
     * @param port
     * @param serviceRootPath
     */
    public ZkNameServiceRegister(@Nonnull ZkClient zkClient, @Nonnull String serverId, @Nonnull String ip, @Nonnegative int port, @Nonnull String serviceRootPath) {
        this(zkClient, serverId, ip, port, serviceRootPath, new ServerNodeProtoCodec());
    }

    /**
     * @param zkClient Zookeeper的客户端
     * @param serverId
     * @param ip
     * @param port
     * @param codec
     */
    public ZkNameServiceRegister(@Nonnull ZkClient zkClient, @Nonnull String serverId, @Nonnull String ip, @Nonnegative int port, @Nonnull String serviceRootPath, @Nonnull ICodec codec) {
        Preconditions.checkArgument(zkClient != null, "zkClient");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(serverId) && serverId.startsWith(ZkConstants.SERVER_NODE_PREFIX), "serverId");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(ip), "ip");
        Preconditions.checkArgument(port > 0, "port");
        Preconditions.checkArgument(codec != null, "codec");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(serviceRootPath) && serviceRootPath.startsWith("/"), "serviceRootPath");
        this.zkClient = zkClient;
        this.serverId = serverId;
        this.ip = ip;
        this.port = port;
        this.serviceRootPath = serviceRootPath;
        this.nodePath = this.serviceRootPath + "/" + this.serverId;
        this.services = Sets.newHashSet();
        this.registerToken = UUID.randomUUID().toString();
        this.codec = codec;

        watcher = new RegisterWatcher();
        this.zkClient.register(watcher);
        this.zkClient.registerExpirationHandler(new Runnable() {
            @Override
            public void run() {
                LOGGER.warn("ZkClient has expired,register all services");
                registerAndNotify();
            }
        });
    }

    @Override
    public synchronized void addService(@Nonnull String service) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(service), "service");
        this.services.add(service);
    }

    @Override
    public synchronized void removeService(@Nonnull String service) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(service), "service");
        this.services.remove(service);
    }

    @Override
    public void addListener(@Nonnull IEventListener<NSRegisterEvent> listener) {
        this.listeners.put(listener, listener);
    }

    @Override
    public synchronized boolean register() {
        LOGGER.info("Register " + this);
        try {
            final ZooKeeper zooKeeper = this.zkClient.get(ZkConstants.CONNECTION_TIMEOUT);
            if (zooKeeper.exists(this.serviceRootPath, false) == null) {
                LOGGER.error("The service root path {} doesn't exist,give up the register.");
                return false;
            }

            NamingServices.NSServer.Builder builder = NamingServices.NSServer.newBuilder();
            builder.setId(this.serverId).setIp(this.ip).setPort(this.port).setRegisterToken(this.registerToken);
            builder.addAllServices(this.services);
            byte[] data = codec.encode(builder.build());

            if (zooKeeper.exists(nodePath, true) != null) {
                LOGGER.warn("The node {} already exists,check the data", nodePath);
                Stat preStat = new Stat();
                byte[] preData = zooKeeper.getData(nodePath, false, preStat);
                if (preData != null && preData.length > 0) {
                    NamingServices.NSServer preServer = (NamingServices.NSServer) codec.decode(preData);
                    LOGGER.info("The exited node register token is {},our token is {}.", preServer.getRegisterToken(), this.registerToken);
                    if (!this.registerToken.equals(preServer.getRegisterToken())) {
                        LOGGER.warn("The exited node register token is {},but our token is {},stop register.", preServer.getRegisterToken(), this.registerToken);
                        return false;
                    }
                    Stat stat = zooKeeper.setData(nodePath, data, preStat.getVersion());
                    LOGGER.info("Set server data for {},stat: {} ", nodePath, stat);
                    return stat != null;
                }
                return false;
            } else {
                LOGGER.warn("The node {} doesn't exist,register it.", nodePath);
                List<ACL> acls = ZkUtils.createACL(zkClient.getZkUser(),zkClient.getZkPassword());
                String path = zooKeeper.create(nodePath, data, acls, EPHEMERAL);
                if (!nodePath.equals(path)) {
                    LOGGER.warn("The created path is {},but it should be {}", nodePath, path);
                }
                return true;
            }
        } catch (Exception e) {
            LOGGER.error("Register error.", e);
        }
        return false;
    }

    @Override
    public synchronized boolean unRegister() {
        LOGGER.info("unRegister " + this);
        start = false;
        this.zkClient.unregister(watcher);
        try {
            final ZooKeeper zooKeeper = this.zkClient.get(ZkConstants.CONNECTION_TIMEOUT);
            if (zooKeeper == null) {
                LOGGER.warn("zookeeper is null,skip unregister");
                return false;
            }
            zooKeeper.delete(nodePath, -1);
            return true;
        } catch (Exception e) {
            LOGGER.error("unRegister error", e);
        }
        return false;
    }

    private void triggerEvent(NSRegisterEvent event) {
        EventUtil.triggerEvent(this.listeners.values(), event);
    }

    /**
     *
     */
    private synchronized void registerAndNotify() {
        if (!start) {
            LOGGER.info("Stop register the servers,because start = " + start);
            return;
        }
        LOGGER.info("Register the server.");
        boolean result = register();
        NSRegisterEventType type = result ? NSRegisterEventType.REG_SUCCESS : NSRegisterEventType.REG_FAIL;
        triggerEvent(new NSRegisterEvent(type, null));
    }

    private class RegisterWatcher implements Watcher {
        @Override
        public void process(WatchedEvent event) {
            if (event.getType() == Event.EventType.None) {
                return;
            }
            final String path = event.getPath();
            if (Strings.isNullOrEmpty(path)) {
                return;
            }

            if (!path.equals(nodePath)) {
                return;
            }

            try {
                ZooKeeper zooKeeper = zkClient.get(ZkConstants.CONNECTION_TIMEOUT);
                if (zooKeeper == null) {
                    LOGGER.warn("zookeeper is null,skip process");
                    return;
                }
                zooKeeper.exists(nodePath, true);
            } catch (Exception e) {
                LOGGER.error("Rewatch the node fail.", e);
                return;
            }
            LOGGER.info("Event:" + event);
            switch (event.getType()) {
                case NodeDeleted:
                    //节点被删除了,尝试重新注册
                    registerAndNotify();
                    break;
                default:
                    break;
            }
        }
    }
}

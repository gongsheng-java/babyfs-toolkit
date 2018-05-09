package com.babyfs.tk.galaxy.register.impl;

import com.alibaba.fastjson.JSON;
import com.babyfs.tk.commons.enums.ShutdownOrder;
import com.babyfs.tk.commons.service.LifeServiceSupport;
import com.babyfs.tk.commons.utils.ListUtil;
import com.babyfs.tk.commons.zookeeper.BackoffHelper;
import com.babyfs.tk.galaxy.register.IServcieRegister;
import com.babyfs.tk.galaxy.register.ServiceServer;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 基于zk的服务注册
 */
@Order
@ShutdownOrder
public class ZkServiceRegister extends LifeServiceSupport implements IServcieRegister {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZkServiceRegister.class);
    /**
     * 本机IP
     */
    private final String serverIP;
    /**
     * 本地服务的端口
     */
    private final int serverPort;
    /**
     * zk注册的根路径
     */
    private final String serverRoot;

    private final CuratorFramework curator;

    private final BackoffHelper backoffHelper = new BackoffHelper();

    /**
     * zk操作的token
     */
    private final String regToken;
    /**
     * 服务接口
     */
    private Set<String> interfaceNames = Sets.newConcurrentHashSet();

    private volatile boolean running;

    private final TreeCache cache;
    private TreeCacheListener listener;

    /**
     * @param curator
     * @param serverPort
     * @param serverRoot
     */
    public ZkServiceRegister(CuratorFramework curator, String serverIP, int serverPort, String serverRoot) {
        Preconditions.checkArgument(serverPort > 0, "serverPort");
        this.curator = Preconditions.checkNotNull(curator);
        this.serverPort = serverPort;
        this.serverRoot = Preconditions.checkNotNull(serverRoot);
        this.serverIP = Preconditions.checkNotNull(serverIP);
        this.regToken = UUID.randomUUID().toString();
        this.running = false;
        this.cache = new TreeCache(this.curator, getNodePath());
    }

    @Override
    public synchronized void addServices(List<String> serviceNames) {
        if (ListUtil.isEmpty(serviceNames)) {
            return;
        }
        this.interfaceNames.addAll(serviceNames);
    }

    @Override
    public synchronized void removeServices(List<String> serviceNames) {
        if (ListUtil.isEmpty(serviceNames)) {
            return;
        }
        this.interfaceNames.removeAll(serviceNames);
    }

    @Override
    public synchronized void updateRegister() {
        this.doRegister();
    }

    @Override
    protected synchronized void execStart() {
        if (running) {
            return;
        }

        super.execStart();
        listener = (curatorFramework, treeCacheEvent) -> {
            TreeCacheEvent.Type type = treeCacheEvent.getType();
            ChildData data = treeCacheEvent.getData();
            LOGGER.info("event type:{},path:{}", type, data != null ? data.getPath() : null);
            if (type == TreeCacheEvent.Type.NODE_REMOVED || type == TreeCacheEvent.Type.CONNECTION_RECONNECTED) {
                LOGGER.warn("{} registe all servcies", type);
                this.backoffHelper.doUntilSuccessAtExecutor((input -> {
                    return doRegister();
                }));
            }
        };
        cache.getListenable().addListener(listener);

        try {
            cache.start();
            if (!this.interfaceNames.isEmpty()) {
                if (!this.backoffHelper.doUntilSuccess(input -> doRegister(), TimeUnit.SECONDS.toMillis(5))) {
                    LOGGER.error("registe services fail");
                }
            }
            running = true;
        } catch (Exception e) {
            LOGGER.error("cache start fail", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    protected synchronized void execStop() {
        try {
            if (!running) {
                return;
            }

            cache.close();
            super.execStop();

            String nodePath = getNodePath();
            LOGGER.info("will delete node {}", nodePath);
            try {
                byte[] data = curator.getData().forPath(nodePath);
                if (data == null) {
                    LOGGER.warn("can't get data for node {}", nodePath);
                } else {
                    ServiceServer server = JSON.parseObject(data, ServiceServer.class);
                    if (!Objects.equals(server.getToken(), this.regToken)) {
                        LOGGER.warn("{} register token  {} != local token {} for node {},can't delete", nodePath, server.getToken(), this.regToken);
                    } else {
                        LOGGER.info("delete node {}", nodePath);
                        curator.delete().guaranteed().forPath(nodePath);
                    }
                }
            } catch (Exception e) {
                LOGGER.error("unregister fail", e);
            }
        } finally {
            if (this.listener != null) {
                cache.getListenable().removeListener(listener);
            }
            this.listener = null;
            running = false;
        }
    }

    private synchronized boolean doRegister() {
        final String nodePath = getNodePath();
        LOGGER.info("register servcie to {}", nodePath);

        try {
            ServiceServer server = new ServiceServer(this.regToken, this.serverIP, this.serverPort);
            server.addService(this.interfaceNames);
            byte[] data = JSON.toJSONBytes(server);
            if (exist(nodePath)) {
                byte[] preData = curator.getData().forPath(nodePath);
                if (preData != null) {
                    ServiceServer preServer = JSON.parseObject(preData, ServiceServer.class);
                    if (!Objects.equals(preServer.getToken(), this.regToken)) {
                        LOGGER.warn("{} register token  {} != local token {},can't update", nodePath, server.getToken(), this.regToken);
                        return false;
                    }
                }
                return curator.setData().forPath(nodePath, data) != null;
            } else {
                return curator.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(nodePath, data) != null;
            }
        } catch (Exception e) {
            LOGGER.error("do register fail", e);
        }
        return false;
    }


    private boolean exist(String path) throws Exception {
        Stat stat = curator.checkExists().forPath(path);
        return stat != null;
    }

    private String getNodePath() {
        return this.serverRoot + "/" + getServerID();
    }

    private String getServerID() {
        return this.serverIP + ":" + serverPort;
    }
}

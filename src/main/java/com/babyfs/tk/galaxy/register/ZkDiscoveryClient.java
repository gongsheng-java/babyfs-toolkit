package com.babyfs.tk.galaxy.register;

import com.babyfs.tk.galaxy.constant.RpcConstant;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 基于zk的服务发现客户端
 */
public final class ZkDiscoveryClient implements IDiscoveryClient, ILifeCycle {

    private final String appName;

    private final int port;

    private final CuratorFramework curator;

    private final Map<String, List<ServiceInstance>> providerMapList = new ConcurrentHashMap<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(ZkDiscoveryClient.class);

    public ZkDiscoveryClient(CuratorFramework curator, String appName, int port) {
        this.appName = appName;
        this.port = port;
        this.curator = curator;
    }

    @Override
    public ServiceInstance getLocalServiceInstance() {
        return new ServiceInstance(appName, getLocalIp()
                , port);
    }

    private String getLocalIp() {
        try {
            for (Enumeration<NetworkInterface> enumNic = NetworkInterface
                    .getNetworkInterfaces(); enumNic.hasMoreElements(); ) {
                NetworkInterface ifc = enumNic.nextElement();
                if (ifc.isUp()) {
                    for (Enumeration<InetAddress> enumAddr = ifc
                            .getInetAddresses(); enumAddr.hasMoreElements(); ) {
                        InetAddress address = enumAddr.nextElement();
                        if (address instanceof Inet4Address
                                && !address.isLoopbackAddress()) {
                            return address.getHostAddress();
                        }
                    }
                }
            }
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return null;
        } catch (IOException e) {
            LOGGER.warn("Unable to find non-loopback address", e);
            return null;
        }
    }

    @Override
    public List<ServiceInstance> getInstancesByAppName(String appName) {
        if (!providerMapList.isEmpty() && providerMapList.containsKey(appName)) {
            return providerMapList.get(appName);
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * 刷新并且返回指定应用的可用实例列表
     *
     * @param appName 应用名称
     * @return
     */
    private void refresh(String appName) {
        String appPath = RpcConstant.DISCOVERY_PREFIX + "/" + appName;
        List<String> hosts = getChildren(appPath);
        //线程安全的list
        List<ServiceInstance> instances = new CopyOnWriteArrayList<>();
        if (CollectionUtils.isEmpty(hosts)) {
            LOGGER.error("the server:{} has no provider", appName);
            providerMapList.remove(appName);
            return;
        }
        for (String string : hosts) {
            String[] parts = string.split(":");
            if (parts.length != 2) {
                LOGGER.error("error host:{}", string);
                continue;
            }
            ServiceInstance serviceInstance = new ServiceInstance(appName, parts[0], Integer.parseInt(parts[1].substring(0, 4)));
            if (!instances.contains(serviceInstance)) {
                instances.add(serviceInstance);
            }
        }
        providerMapList.put(appName, instances);
        return;
    }

    /**
     * 获取指定path的子节点
     *
     * @param path
     * @return
     */
    private List<String> getChildren(String path) {
        try {
            return curator.getChildren().forPath(path);
        } catch (Exception e) {
            LOGGER.error("ZkDiscoveryClient@getChildren fail", e);
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public void register() throws Exception {
        create(getLocalZkPath());
    }

    /**
     * 在指定路径下创建节点
     *
     * @param path zk节点路径
     * @throws Exception
     */
    private void create(String path) throws Exception {
        curator.create()
                .creatingParentContainersIfNeeded()
                .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                .forPath(path);
    }

    @Override
    public void watch() throws Exception {
        connect(RpcConstant.DISCOVERY_PREFIX);
    }

    @Override
    public void start() throws Exception {
        watch();
        register();
        //增加ConnectionStateListener
        ConnectionStateListener connectionStateListener = (curatorFramework, connectionState) -> {
            if (connectionState == ConnectionState.RECONNECTED) {
                LOGGER.error("[负载均衡]zk重连");
                while (true) {
                    try {
                        if (curatorFramework.getZookeeperClient().blockUntilConnectedOrTimedOut()) {
                            create(getLocalZkPath());
                            //curator treeCahce支持连接断开后从新监听，所以不用再一次添加监听器
                            break;
                        }
                    } catch (Exception e) {
                        LOGGER.error("reconnect zookeeper fail", e);
                    }
                }
            }
        };
        curator.getConnectionStateListenable().addListener(connectionStateListener);
        //增加shutDownHook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.error("deRegister ......");
            deleteIfExit(getLocalZkPath());
        }));
    }

    /**
     * 如果指定path下的节点存在删除
     *
     * @param path zk节点路径
     */
    private void deleteIfExit(String path) {
        try {
            if (!exit(path)) {
                LOGGER.warn("the path not exit");
            } else {
                curator.delete().forPath(path);
            }
        } catch (Exception e) {
            LOGGER.error("zk delete node fail path:{}", path, e);
        }
    }

    private boolean exit(String path) throws Exception {
        Stat stat = curator.checkExists().forPath(path);
        return stat != null;
    }

    /**
     * @param watchPath 监听的地址
     * @throws Exception
     */
    private void connect(final String watchPath) throws Exception {
        TreeCache cache = new TreeCache(curator, watchPath);
        TreeCacheListener listener = (curatorFramework, treeCacheEvent) -> {
            LOGGER.info("事件类型：" + treeCacheEvent.getType() +
                    " | 路径：" + (null != treeCacheEvent.getData() ? treeCacheEvent.getData().getPath() : null));
            if (treeCacheEvent.getData() != null) {
                String path = treeCacheEvent.getData().getPath();
                String[] strings = path.split("/");
                if (strings.length > 4) {
                    refresh(strings[3]);
                }
            }
        };
        cache.getListenable().addListener(listener);
        cache.start();
    }

    private String getLocalZkPath() {
        return RpcConstant.DISCOVERY_PREFIX + "/" + appName + "/" + getLocalIp() + ":" + port;
    }

}

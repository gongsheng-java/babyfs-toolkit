package com.babyfs.tk.galaxy.register;

import com.babyfs.tk.galaxy.RpcException;
import com.babyfs.tk.galaxy.constant.RpcConstant;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 基于zk的服务发现客户端
 */
final class ZkDiscoveryClient implements IDiscoveryClient, ILifeCycle {

    private final IRpcConfigService properties;

    private final CuratorFramework curator;

    private final Map<String, List<ServiceInstance>> providerMapList = new ConcurrentHashMap<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(ZkDiscoveryClient.class);

    public ZkDiscoveryClient(IRpcConfigService discoveryProperties, CuratorFramework curator) {
        this.properties = discoveryProperties;
        this.curator = curator;
    }

    @Override
    public ServiceInstance getLocalServiceInstance() {
        return new ServiceInstance(properties.getAppName(), getLocalIp()
                , properties.getPort());
    }

    private String getLocalIp() {
//        try {
//            for (Enumeration<NetworkInterface> enumNic = NetworkInterface
//                    .getNetworkInterfaces(); enumNic.hasMoreElements(); ) {
//                NetworkInterface ifc = enumNic.nextElement();
//                if (ifc.isUp()) {
//                    for (Enumeration<InetAddress> enumAddr = ifc
//                            .getInetAddresses(); enumAddr.hasMoreElements(); ) {
//                        InetAddress address = enumAddr.nextElement();
//                        if (address instanceof Inet4Address
//                                && !address.isLoopbackAddress()) {
//                            return address.getHostAddress();
//                        }
//                    }
//                }
//            }
//            return InetAddress.getLocalHost().getHostAddress();
//        } catch (UnknownHostException e) {
//            return null;
//        } catch (IOException e) {
//            LOGGER.warn("Unable to find non-loopback address", e);
//            return null;
//        }
        return "127.0.0.1";
    }

    @Override
    public List<ServiceInstance> getInstances(String appName) {
        if (!providerMapList.isEmpty() && providerMapList.containsKey(appName)) {
            return providerMapList.get(appName);
        }
        return refreshAndGet(appName);
    }

    /**
     * 刷新并且返回指定应用的可用实例列表
     *
     * @param appName 应用名称
     * @return
     */
    private List<ServiceInstance> refreshAndGet(String appName) {
        String path = RpcConstant.DISCOVERY_PREFIX + "/" + appName;
        List<String> hosts = getChildren(path);
        List<ServiceInstance> instances = new CopyOnWriteArrayList<>();
        if (CollectionUtils.isEmpty(hosts)) {
            LOGGER.error("the server:{} has no provider", appName);
            return Collections.EMPTY_LIST;
        }
        for (String string : hosts) {
            String[] parts = string.split(":");
            instances.add(new ServiceInstance(appName, parts[0], Integer.parseInt(parts[1])));
        }
        if (CollectionUtils.isEmpty(instances)) {
            LOGGER.error("the server:{} has no provider", appName);
            return Collections.EMPTY_LIST;
        }
        providerMapList.put(appName, instances);
        return instances;
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
        String path = RpcConstant.DISCOVERY_PREFIX + "/" + properties.getAppName() + "/" + getLocalIp() + ":" + properties.getPort();
        create(path);
    }

    /**
     * 在指定路径下创建节点
     *
     * @param path
     * @throws Exception
     */
    private void create(String path) throws Exception {
        try {
            curator.create()
                    .creatingParentContainersIfNeeded()
                    .withMode(CreateMode.EPHEMERAL)
                    .forPath(path);
        } catch (Exception e) {
            LOGGER.error("create zk node  fail path:{}", path, e);
            throw new RpcException("create zk node fail .path:" + path, e);
        }
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
                LOGGER.error("[负载均衡失败]zk连接断开");
                while (true) {
                    try {
                        if (curatorFramework.getZookeeperClient().blockUntilConnectedOrTimedOut()) {
                            register();
                            watch();
                            break;
                        }
                    } catch (Exception e) {
                        LOGGER.error("reconnect zookeeper fail", e);
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        LOGGER.error("thread sleep InterruptedException", e);
                    }
                }
            }
        };
        curator.getConnectionStateListenable().addListener(connectionStateListener);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.error("deRegister ......");
            String path = RpcConstant.DISCOVERY_PREFIX + "/" + properties.getAppName() + "/" + getLocalIp() + ":" + properties.getPort();
            delete(path);
        }));
    }

    /**
     * 删除指定path下的节点
     *
     * @param path
     */
    private void delete(String path) {
        try {
            curator.delete().forPath(path);
        } catch (Exception e) {
            LOGGER.error("zk delete node fail path:{}", path, e);
        }
    }

    /**
     * zk监听指定path
     *
     * @param PATH 监听的地址
     * @throws Exception
     */
    private void connect(final String PATH) throws Exception {
        TreeCache cache = new TreeCache(curator, PATH);
        TreeCacheListener listener = (curatorFramework, treeCacheEvent) -> {
            LOGGER.debug("事件类型：" + treeCacheEvent.getType() +
                    " | 路径：" + (null != treeCacheEvent.getData() ? treeCacheEvent.getData().getPath() : null));
            if (treeCacheEvent.getData() != null) {
                String path = treeCacheEvent.getData().getPath();
                String[] strings = path.split("/");
                if (strings.length == 4) {
                    refreshAndGet(strings[3]);
                }
            }
        };
        cache.getListenable().addListener(listener);
        cache.start();
    }

}

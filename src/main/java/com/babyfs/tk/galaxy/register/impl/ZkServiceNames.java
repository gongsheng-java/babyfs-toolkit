package com.babyfs.tk.galaxy.register.impl;

import com.alibaba.fastjson.JSON;
import com.babyfs.tk.commons.enums.ShutdownOrder;
import com.babyfs.tk.commons.service.LifeServiceSupport;
import com.babyfs.tk.commons.zookeeper.BackoffHelper;
import com.babyfs.tk.galaxy.register.IServiceNames;
import com.babyfs.tk.galaxy.register.ServiceServer;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.data.Stat;
import org.elasticsearch.common.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.babyfs.tk.galaxy.register.ServiceServer.DEFAULT_VERSION;

/**
 * 基于zk的服务发现客户端
 */
@Order(-2)
@ShutdownOrder(100)
public final class ZkServiceNames extends LifeServiceSupport implements IServiceNames {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZkServiceNames.class);
    /**
     * curator
     */
    private final CuratorFramework curator;
    /**
     * zk注册的根路径
     */
    private final String serverRoot;
    /**
     * key service name,value:server list
     */
    private volatile ServiceServerCache serviceServers = new ServiceServerCache();
    /**
     * 监控serverRoot的cache
     */
    private final TreeCache cache;

    private final BackoffHelper backoffHelper = new BackoffHelper();
    private volatile boolean running;

    private final ServiceServers emptyServiceServers = new ServiceServers(null);

    /**
     * @param curator
     * @param serverRoot
     */
    public ZkServiceNames(CuratorFramework curator, String serverRoot) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(serverRoot) && serverRoot.startsWith("/"), "serverRoot");
        this.curator = Preconditions.checkNotNull(curator);
        this.serverRoot = serverRoot;
        this.cache = new TreeCache(this.curator, this.serverRoot);
        this.running = false;
    }


    @Override
    public ServerGroup findServers(String servcieName) {
        return serviceServers.serviceServers.getOrDefault(servcieName, emptyServiceServers).getServers();
    }


    /**
     * 启动
     */
    @Override
    protected synchronized void execStart() {
        if (running) {
            return;
        }

        super.execStart();

        TreeCacheListener listener = (curatorFramework, treeCacheEvent) -> {
            Type type = treeCacheEvent.getType();
            ChildData data = treeCacheEvent.getData();
            LOGGER.info("event type:{},path:{}", type, (data != null ? data.getPath() : null));
            switch (type) {
                case NODE_ADDED: {
                    ServiceServer server = parseServiceServer(data);
                    serviceServers.update(server);
                    break;
                }
                case NODE_UPDATED: {
                    ServiceServer server = parseServiceServer(data);
                    serviceServers.update(server);
                    break;
                }
                case NODE_REMOVED: {
                    serviceServers.removeServer(parseServiceServerFromPath(data));
                    break;
                }
                case CONNECTION_RECONNECTED:
                    this.backoffHelper.doUntilSuccessAtExecutor(input -> loadServices());
                    break;
                default:
                    LOGGER.info("skip event type:{}", type);
                    break;
            }
        };

        cache.getListenable().addListener(listener);
        try {
            cache.start();
            if (!this.backoffHelper.doUntilSuccess(input -> loadServices(), TimeUnit.SECONDS.toMillis(5))) {
                LOGGER.error("load servcies fail");
            } else {
                LOGGER.info("serviceServers:{}", this.serviceServers);
            }
            running = true;
        } catch (Exception e) {
            LOGGER.error("cache start fail", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    protected synchronized void execStop() {
        cache.close();
        super.execStop();
    }

    /**
     * 加载所有已经注册的服务
     *
     * @return
     */
    private synchronized boolean loadServices() {
        try {
            final ServiceServerCache newCache = new ServiceServerCache();

            ChildData currentData = cache.getCurrentData(this.serverRoot);
            if (currentData == null) {
                return false;
            }

            Stat stat = currentData.getStat();
            int numChildren = stat.getNumChildren();

            LOGGER.info("load children from {},numChildren:{}", this.serverRoot, numChildren);

            Map<String, ChildData> children = cache.getCurrentChildren(this.serverRoot);
            if (numChildren > 0 && (children == null || children.isEmpty())) {
                return false;
            }
            LOGGER.info("load children data from {},numChildren:{}", this.serverRoot, children.size());

            for (Map.Entry<String, ChildData> entry : children.entrySet()) {
                ChildData childData = entry.getValue();
                if (childData == null) {
                    continue;
                }
                ServiceServer server = parseServiceServer(childData);
                if (server == null) {
                    continue;
                }
                newCache.update(server);
            }
            this.serviceServers = newCache;
            return true;
        } catch (Exception e) {
            LOGGER.error("load all fail", e);
        }
        return false;
    }

    /**
     * 从{@link ChildData#getData()}中解析
     *
     * @param data
     * @return
     */
    private ServiceServer parseServiceServer(ChildData data) {
        if (data == null || data.getData() == null || data.getData().length == 0) {
            return null;
        }
        try {
            return JSON.parseObject(data.getData(), ServiceServer.class);
        } catch (Exception e) {
            LOGGER.error("parse json {} fail", new String(data.getData()), e);
        }
        return null;
    }

    /**
     * 从{@link ChildData#getPath()}中解析
     *
     * @param data
     * @return
     */
    private ServiceServer parseServiceServerFromPath(ChildData data) {
        if (data == null || data.getPath() == null) {
            return null;
        }
        String nodeFromPath = ZKPaths.getNodeFromPath(data.getPath());
        List<String> strings = Splitter.on(":").omitEmptyStrings().splitToList(nodeFromPath);

        if (strings.size() != 2) {
            return null;
        }
        return new ServiceServer(null, strings.get(0), Integer.parseInt(strings.get(1)), DEFAULT_VERSION);
    }

}

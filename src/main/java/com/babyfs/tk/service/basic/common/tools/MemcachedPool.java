package com.babyfs.tk.service.basic.common.tools;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.cache.*;
import net.spy.memcached.AddrUtil;
import net.spy.memcached.DefaultConnectionFactory;
import net.spy.memcached.MemcachedClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 为使用memcache协议通讯的客户端提供了一套通用的池，减少不必要的memcachedClient重复创建
 */
public class MemcachedPool {
    private static final Logger LOGGER = LoggerFactory.getLogger(MemcachedPool.class);
    private final LoadingCache<String, MemcachedClient> clientCache;

    public MemcachedPool(DefaultConnectionFactory connectionFactory) {
        clientCache = CacheBuilder.newBuilder().removalListener(new ShutdownListener()).build(new MemcacheClientLoader(connectionFactory));
    }

    public MemcachedPool() {
        clientCache = CacheBuilder.newBuilder().removalListener(new ShutdownListener()).build(new MemcacheClientLoader());
    }

    /**
     * 这个类用来监听当某个memcachedClient销毁的时候，提供处理方法
     * 但是一般情况下memcachedClient销毁都是再服务器关闭时候，并且为了防止错误第关闭了client value.shutdown()，不在这里做shutdown操作
     */
    private static final class ShutdownListener implements RemovalListener<String, MemcachedClient> {
        @Override
        public void onRemoval(RemovalNotification<String, MemcachedClient> notification) {
            MemcachedClient value = notification.getValue();
            LOGGER.info("Removed Memcached client:" + value);
        }
    }

    /**
     * 根据名称获得一个连接，如果没有则创建一个连接
     * key的形式可以是：
     * （1）192.168.12.107:8080这种形式适合共享memcacheClient
     * （2）[yourCacheName]#192.168.12.107:8080这种方式适合单独指定的memcacheClient
     *
     * @param key
     * @return
     * @throws Exception
     */
    public MemcachedClient getMemcachedClient(String key) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(key) && key.indexOf('#') != 0, "memcacheKey1");
        try {
            return clientCache.get(key);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private static final class MemcacheClientLoader extends CacheLoader<String, MemcachedClient> {
        DefaultConnectionFactory connectionFactory;

        public MemcacheClientLoader() {
            this.connectionFactory = new DefaultConnectionFactory();
        }

        public MemcacheClientLoader(DefaultConnectionFactory connectionFactory) {
            this.connectionFactory = connectionFactory;
        }

        /**
         * cache没有对应的memcachedClient时候根据key中的信息创建一个新的memcachedClient
         *
         * @param key
         * @return
         * @throws Exception
         */
        @Override
        public MemcachedClient load(String key) throws Exception {
            Preconditions.checkNotNull(key);
            String[] infos = key.split("#");
            String address = key;
            Preconditions.checkArgument((infos.length == 1 || infos.length == 2), "invalid key:" + key);
            if (infos.length == 2) {
                address = infos[1];
            }
            return new MemcachedClient(connectionFactory, AddrUtil.getAddresses(address));

        }
    }

}

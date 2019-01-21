package com.babyfs.tk.service.basic.redis.client;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.babyfs.tk.commons.utils.ListUtil;
import com.babyfs.tk.service.basic.ServiceLoader;
import com.babyfs.tk.service.basic.redis.IRedis;
import com.babyfs.tk.service.basic.xml.client.Group;
import com.babyfs.tk.service.basic.xml.client.ServerElement;
import com.babyfs.tk.service.basic.xml.client.ServiceGroup;
import com.babyfs.tk.service.basic.xml.server.Server;
import com.babyfs.tk.service.basic.xml.server.Servers;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedisPool;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Shard Redis的服务加载器
 */
public class ShardedRedisServiceLoaderImpl extends ServiceLoader<IRedis> {
    private final Servers servers;
    private final ServiceGroup serviceGroup;

    private final Logger LOGGER = LoggerFactory.getLogger(ShardedRedisServiceLoaderImpl.class);
//    private final static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, 5, 0,
//            TimeUnit.DAYS, new LinkedBlockingDeque<>(1), new ThreadFactoryBuilder().
//            setDaemon(true).setNameFormat("redis-monitor-%d").build());

    /**
     * 配置
     */
    private final RedisConfig redisConfig;

    public ShardedRedisServiceLoaderImpl(@Nonnull RedisConfig redisConfig, @Nonnull Servers servers, @Nonnull ServiceGroup serviceGroup) {
        //server
        Preconditions.checkNotNull(servers, "servers");
        Preconditions.checkNotNull(servers.getServers(), "servers.getServers");
        this.servers = servers;
        //client
        Preconditions.checkNotNull(serviceGroup, "clientConfig");
        this.serviceGroup = serviceGroup;

        Preconditions.checkArgument(redisConfig != null, "Redis Config can't be null!");
        this.redisConfig = redisConfig;
    }

    @Override
    public IRedis load(final String key) throws Exception {
        Preconditions.checkNotNull(key);
        Group group = serviceGroup.getGroups().get(key);
        Preconditions.checkNotNull(group, "redis group `" + key + "` not defined");
        List<ServerElement> serverList = group.getServerList().getServerElements();
        Preconditions.checkNotNull(serverList, "serverList is null");
        Preconditions.checkArgument(!serverList.isEmpty(), "serverList is empty");
        //获得key对应的服务器集群列表
        List<JedisShardInfo> shards = ListUtil.transform(serverList, new Function<ServerElement, JedisShardInfo>() {
            @Override
            public JedisShardInfo apply(ServerElement input) {
                Preconditions.checkNotNull(input);
                Map<String, Server> serverMap = servers.getServers();
                Server server = serverMap.get(input.getName());
                Preconditions.checkNotNull(server, "Can't find the sever for key:%s,server name:%s", key, input.getName());
                String host = server.getHost();
                String port = server.getPort();
                JedisShardInfo shardInfo = new JedisShardInfo(host, Integer.parseInt(port), redisConfig.getTimeout());
                if (!Strings.isNullOrEmpty(server.getPassword())) {
                    shardInfo.setPassword(server.getPassword());
                }
                return shardInfo;
            }
        });
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(redisConfig.getPoolMaxActive() * serverList.size());
        config.setMaxWaitMillis(redisConfig.getPoolMaxWait());
        config.setMaxIdle(redisConfig.getPoolMaxIdel());
        config.setMinIdle(redisConfig.getPoolMinIdel());
        final ShardedJedisPool pool = new ShardedJedisPool(config, shards);
//        threadPoolExecutor.execute(() -> {
//            for(;;){
//                LOGGER.info("array key:{} - pool getNumActive:{}, getNumIdle:{}, getNumWaiters:{} ", key, pool.getNumActive(), pool.getNumIdle(), pool.getNumWaiters());
//                try {
//                    TimeUnit.SECONDS.sleep(5);
//                } catch (InterruptedException e) {
//                }
//
//            }
//        });
        return new RedisImpl(pool);
    }

    public static void main(String[] args) {
        new ShardedRedisServiceLoaderImpl(null, null, null);
    }
}

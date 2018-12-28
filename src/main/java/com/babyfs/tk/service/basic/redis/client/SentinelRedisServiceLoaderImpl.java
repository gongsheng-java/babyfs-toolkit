package com.babyfs.tk.service.basic.redis.client;

import com.babyfs.tk.commons.utils.ListUtil;
import com.babyfs.tk.service.basic.ServiceLoader;
import com.babyfs.tk.service.basic.redis.IRedis;
import com.babyfs.tk.service.basic.xml.client.Group;
import com.babyfs.tk.service.basic.xml.client.ServerElement;
import com.babyfs.tk.service.basic.xml.client.ServiceGroup;
import com.babyfs.tk.service.basic.xml.server.Server;
import com.babyfs.tk.service.basic.xml.server.Servers;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedisPool;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 哨兵模式的 Redis的服务加载器，通过配置哨兵的地址和哨兵监测的master名称获取可用的redis
 */
public class SentinelRedisServiceLoaderImpl extends ServiceLoader<IRedis> {


    /**
     * 配置
     */
    private final RedisConfig redisConfig;

    public SentinelRedisServiceLoaderImpl(@Nonnull RedisConfig redisConfig) {
        //server
        Preconditions.checkArgument(redisConfig != null, "Redis Config can't be null!");
        this.redisConfig = redisConfig;
    }

    @Override
    public IRedis load(final String key) throws Exception {
        String sentinelAddrKey="sentinel.addr."+key;
        String sentinelMasterKeys="sentinel.masters."+key;

        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(redisConfig.getPoolMaxActive());
        config.setMaxWaitMillis(redisConfig.getPoolMaxWait());
        config.setMaxIdle(redisConfig.getPoolMaxIdel());
        config.setMinIdle(redisConfig.getPoolMinIdel());


        Set<String> sentinels = Sets.newHashSet();
        List<JedisSentinelPool> groupPool = Lists.newArrayList();

        JedisSentinelPool pool = new JedisSentinelPool("dxy",sentinels,config);
        ShardedJedisPool
        groupPool.add(pool);
        return new (pool);
//        Preconditions.checkNotNull(key);
//        Group group = serviceGroup.getGroups().get(key);
//        Preconditions.checkNotNull(group, "redis group `" + key + "` not defined");
//        List<ServerElement> serverList = group.getServerList().getServerElements();
//        Preconditions.checkNotNull(serverList, "serverList is null");
//        Preconditions.checkArgument(!serverList.isEmpty(), "serverList is empty");
//        //获得key对应的服务器集群列表
//        List<JedisShardInfo> shards = ListUtil.transform(serverList, new Function<ServerElement, JedisShardInfo>() {
//            @Override
//            public JedisShardInfo apply(ServerElement input) {
//                Preconditions.checkNotNull(input);
//                Map<String, Server> serverMap = servers.getServers();
//                Server server = serverMap.get(input.getName());
//                Preconditions.checkNotNull(server, "Can't find the sever for key:%s,server name:%s", key, input.getName());
//                String host = server.getHost();
//                String port = server.getPort();
//                JedisShardInfo shardInfo = new JedisShardInfo(host, Integer.parseInt(port), redisConfig.getTimeout());
//                if (!Strings.isNullOrEmpty(server.getPassword())) {
//                    shardInfo.setPassword(server.getPassword());
//                }
//                return shardInfo;
//            }
//        });
//        JedisPoolConfig config = new JedisPoolConfig();
//        config.setMaxTotal(redisConfig.getPoolMaxActive() * serverList.size());
//        config.setMaxWaitMillis(redisConfig.getPoolMaxWait());
//        config.setMaxIdle(redisConfig.getPoolMaxIdel());
//        config.setMinIdle(redisConfig.getPoolMinIdel());
//        ShardedJedisPool pool = new ShardedJedisPool(config, shards);
//        return new RedisImpl(pool);
    }
}

package com.babyfs.tk.service.basic.redis.client;

import com.google.common.base.Preconditions;
import com.babyfs.tk.service.basic.ServiceLoader;
import com.babyfs.tk.service.basic.xml.client.Group;
import com.babyfs.tk.service.basic.xml.client.ServerElement;
import com.babyfs.tk.service.basic.xml.client.ServiceGroup;
import com.babyfs.tk.service.basic.xml.server.Server;
import com.babyfs.tk.service.basic.xml.server.Servers;
import org.apache.commons.lang.StringUtils;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Redis的服务加载器
 */
public class JRedisPoolServiceLoaderImpl extends ServiceLoader<JedisPool> {
    private final Servers servers;
    private final ServiceGroup serviceGroup;

    /**
     * 配置
     */
    private final RedisConfig redisConfig;

    public JRedisPoolServiceLoaderImpl(@Nonnull RedisConfig redisConfig, @Nonnull Servers servers, @Nonnull ServiceGroup serviceGroup) {
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
    public JedisPool load(final String key) throws Exception {
        Group group = serviceGroup.getGroups().get(key);
        Preconditions.checkNotNull(group, "group is null");
        List<ServerElement> serverList = group.getServerList().getServerElements();
        Preconditions.checkNotNull(serverList, "serverList is null");
        Preconditions.checkArgument(!serverList.isEmpty(), "serverList is empty");
        Preconditions.checkState(serverList.size() == 1, "only one server allowd in %s", key);
        ServerElement serverElement = serverList.get(0);
        Server server = Preconditions.checkNotNull(servers.getServers().get(serverElement.getName()));
        String host = server.getHost();
        String password = StringUtils.trimToNull(server.getPassword());
        int port = Integer.parseInt(server.getPort());


        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(redisConfig.getPoolMaxActive() * serverList.size());
        config.setMaxWaitMillis(redisConfig.getPoolMaxWait());
        config.setMaxIdle(redisConfig.getPoolMaxIdel());
        config.setMinIdle(redisConfig.getPoolMinIdel());
        return new JedisPool(config, host, port, redisConfig.getTimeout(), password);
    }

}

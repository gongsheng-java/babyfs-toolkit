package com.babyfs.tk.service.basic.redis.client;

import com.google.common.base.Preconditions;
import com.babyfs.tk.service.basic.ServiceLoader;
import com.babyfs.tk.service.basic.xml.client.Group;
import com.babyfs.tk.service.basic.xml.client.ServerElement;
import com.babyfs.tk.service.basic.xml.client.ServiceGroup;
import com.babyfs.tk.service.basic.xml.server.Server;
import com.babyfs.tk.service.basic.xml.server.Servers;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;
import redis.clients.jedis.*;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

/**
 * Redis的服务加载器
 */
public class JRedisPoolServiceLoaderImpl extends ServiceLoader<JedisPool> {
    private final String HOST_SEPERATE = ",";
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

    /**
     * zhonghuawei 2019-1-1 增加对Sentinel的支持，如果配置的host里有逗号分隔，使用配置sentinel加载
     *
     * @param key
     * @return
     * @throws Exception
     */
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
//        if (!host.isEmpty() && host.indexOf(HOST_SEPERATE) > 0) {
//            return sentinelPool(server.getName(), host, config, redisConfig.getTimeout(), password);
//        }
        return new JedisPool(config, host, port, redisConfig.getTimeout(), password);
    }

    /**
     * 初始化一个哨兵模式
     *
     * @param name
     * @param hosts
     * @param config
     * @return
     */
//    private JedisPool sentinelPool(String name, String hosts, JedisPoolConfig config, int timeout, String password) {
//        Preconditions.checkNotNull(name, "name is null");
//        Preconditions.checkNotNull(hosts, "hosts is null");
//
//        Set<String> sentinels = Sets.newHashSet();
//        // String[] hostList = hosts.replace('，', ',').replace(';', ',').split(",");
//        String[] hostList = hosts.split(HOST_SEPERATE);
//        for (String host : hostList) {
//            if (!host.isEmpty()) {
//                sentinels.add(host);
//            }
//        }
//
//        return new JedisSentinelPool(name, sentinels, config, timeout, password);
//    }

}

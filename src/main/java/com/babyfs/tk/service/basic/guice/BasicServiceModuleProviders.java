package com.babyfs.tk.service.basic.guice;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.babyfs.tk.commons.MapConfig;
import com.babyfs.tk.commons.config.IConfigService;
import com.babyfs.tk.service.basic.INameResourceService;
import com.babyfs.tk.service.basic.common.CommonNameResourceServiceImpl;
import com.babyfs.tk.service.basic.guice.annotation.ServiceRedis;
import com.babyfs.tk.service.basic.redis.Constants;
import com.babyfs.tk.service.basic.redis.IRedis;
import com.babyfs.tk.service.basic.redis.client.JRedisPoolServiceLoaderImpl;
import com.babyfs.tk.service.basic.redis.client.RedisConfig;
import com.babyfs.tk.service.basic.redis.client.ShardedRedisServiceLoaderImpl;
import com.babyfs.tk.service.basic.xml.client.ServiceGroup;
import com.babyfs.tk.service.basic.xml.server.Servers;
import redis.clients.jedis.JedisPool;

import java.util.Map;


/**
 * Basic Service 的各种{@link Provider}定义
 * <p/>
 */
public final class BasicServiceModuleProviders {
    private BasicServiceModuleProviders() {

    }

    /**
     * 构建RedisConfig
     *
     * @param conf
     * @return
     */
    private static RedisConfig buildRedisConfig(Map<String, String> conf) {
        RedisConfig redisConfig = new RedisConfig();
        redisConfig.setTimeout(MapConfig.getInt(Constants.CONF_KEY_TIMEOUT, conf, Constants.DEFAULT_TIMEOUT));
        redisConfig.setPoolMaxIdel(MapConfig.getInt(Constants.CONF_KEY_MAX_IDEL, conf, Constants.DEFAULT_MAX_IDEL));
        redisConfig.setPoolMinIdel(MapConfig.getInt(Constants.CONF_KEY_MIN_IDEL, conf, Constants.DEFAULT_MIN_IDEL));
        redisConfig.setPoolMaxActive(MapConfig.getInt(Constants.CONF_KEY_MAX_ACTIVE, conf, Constants.DEFAULT_MAX_ACTIVE));
        redisConfig.setPoolMaxWait(MapConfig.getLong(Constants.CONF_KEY_MAX_WAIT, conf, Constants.DEFAULT_MAX_WAIT));
        return redisConfig;
    }

    /**
     * 提供{@link IRedis}的Provider
     */
    public static final class ShardedRedisServiceProvider implements Provider<INameResourceService<IRedis>> {
        @Inject
        @ServiceRedis
        private Servers servers;

        @Inject
        @ServiceRedis
        private ServiceGroup serviceGroup;

        /**
         * 全局Key-Value Props配置
         */
        @Inject(optional = true)
        private IConfigService conf;

        @Override
        public INameResourceService<IRedis> get() {
            RedisConfig redisConfig = buildRedisConfig(conf);
            ShardedRedisServiceLoaderImpl redisServiceLoader = new ShardedRedisServiceLoaderImpl(redisConfig, servers, serviceGroup);
            return new CommonNameResourceServiceImpl<>(redisServiceLoader);
        }
    }

    /**
     * 提供{@link JedisPool}的Provider
     */
    public static final class JedisPoolServiceProvider implements Provider<INameResourceService<JedisPool>> {
        @Inject
        @ServiceRedis
        private Servers servers;

        @Inject
        @ServiceRedis
        private ServiceGroup serviceGroup;

        /**
         * 全局Key-Value Props配置
         */
        @Inject(optional = true)
        private IConfigService conf;

        @Override
        public INameResourceService<JedisPool> get() {
            RedisConfig redisConfig = buildRedisConfig(conf);
            JRedisPoolServiceLoaderImpl redisServiceLoader = new JRedisPoolServiceLoaderImpl(redisConfig, servers, serviceGroup);
            return new CommonNameResourceServiceImpl<>(redisServiceLoader);
        }

    }
}

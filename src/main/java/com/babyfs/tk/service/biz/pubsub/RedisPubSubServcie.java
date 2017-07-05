package com.babyfs.tk.service.biz.pubsub;

import com.alibaba.fastjson.JSONObject;
import com.babyfs.tk.commons.config.IConfigService;
import com.babyfs.tk.commons.service.LifeServiceSupport;
import com.babyfs.tk.commons.thread.NamedThreadFactory;
import com.babyfs.tk.commons.utils.MapUtil;
import com.babyfs.tk.commons.utils.ThreadUtil;
import com.babyfs.tk.service.basic.INameResourceService;
import com.babyfs.tk.service.basic.guice.annotation.ServiceRedis;
import com.babyfs.tk.service.biz.cache.LocalCacheChangeMessage;
import com.babyfs.tk.service.biz.cache.LocalCacheType;
import com.babyfs.tk.service.biz.constants.Const;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * order使用默认优先级最低,最后一个启动
 */
@Order
public class RedisPubSubServcie extends LifeServiceSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisPubSubServcie.class);
    protected final INameResourceService<JedisPool> redisService;
    private final ExecutorService subscribeExecutor = Executors.newCachedThreadPool(new NamedThreadFactory("redis-subscribe"));

    private final Object subscribedLock = new Object();
    private final Set<JedisPubSub> subscribed = Sets.newHashSet();
    private volatile boolean stop = false;
    private final String deaultRedisGroup;
    private final String defaultChannelName;

    @Inject(optional = true)
    @Named(Const.EVENTBUS_SUBSCRIBE)
    EventBus eventBus;

    @Inject
    public RedisPubSubServcie(@ServiceRedis INameResourceService<JedisPool> redisService, IConfigService configService) {
        this.redisService = Preconditions.checkNotNull(redisService);
        this.deaultRedisGroup = MapUtil.get(configService, Const.CONF_PUBSUB_DEFAULT_REDISGROUP, "default_pubsub");
        this.defaultChannelName = MapUtil.get(configService, Const.CONF_PUBSUB_DEFAULT_CHANNELNAME, "default_channel");
    }

    /**
     * 发送消息到redisGroup的channel中
     *
     * @param redisGroup  Redis组名称,redisGroup中只能配置一个redis server,not null
     * @param channelName 频道名称,not null
     * @param message     消息,not null
     * @return 操作是否成功
     */
    public boolean publish(String redisGroup, String channelName, String message) {
        Preconditions.checkNotNull(redisGroup);
        Preconditions.checkNotNull(channelName);
        Preconditions.checkNotNull(message);
        try (Jedis jedis = this.redisService.get(redisGroup).getResource()) {
            jedis.publish(channelName, message);
            return true;
        } catch (Exception e) {
            LOGGER.error("publish message:`" + message + "` to channel:`" + channelName + "`,redis group:`" + redisGroup + "` fail", e);
            return false;
        }
    }

    /**
     * 发送消息到默认的group和channel中
     *
     * @param localCacheType
     * @param id
     * @return
     */
    public boolean publishToDefaultGroup(LocalCacheType localCacheType, Object id) {
        return publish(getDeaultRedisGroup(), getDefaultChannelName(), JSONObject.toJSONString(LocalCacheChangeMessage.newMessage(localCacheType, id)));
    }

    /**
     * 订阅频道
     *
     * @param redisGroup Redis组名称,redisGroup中只能配置一个redis server,not null
     * @param channels   频道名称,not null
     * @return
     */
    public boolean subscribe(String redisGroup, String... channels) {
        Preconditions.checkNotNull(redisGroup);
        Preconditions.checkNotNull(channels);
        String join = Joiner.on(",").join(channels);
        try {
            this.subscribeExecutor.submit(() -> {
                while (!stop) {
                    try (Jedis jedis = this.redisService.get(redisGroup).getResource()) {
                        final JedisPubSub jedisPubSub = new JedisPubSub() {
                            @Override
                            public void onMessage(String channel, String message) {
                                LOGGER.info("receive message:`{}` on channel:`{}`", message, channel);
                                if (channel == null || message == null) {
                                    return;
                                }
                                // 发送事件通知
                                if (eventBus != null) {
                                    eventBus.post(new PubSubChannelEvent(channel, message));
                                }
                            }

                            @Override
                            public void onSubscribe(String channel, int subscribedChannels) {
                                LOGGER.info("receive subscribe on channel:`{}`,subscribedChannels:{}", channel, subscribedChannels);
                            }

                            @Override
                            public void onUnsubscribe(String channel, int subscribedChannels) {
                                LOGGER.info("receive unsubscribe on channel:`{}`,subscribedChannels:{}", channel, subscribedChannels);
                            }
                        };

                        //记录订阅
                        addSubscribed(jedisPubSub);

                        try {
                            LOGGER.info("subscribe channel:{}", join);
                            jedis.subscribe(jedisPubSub, channels);
                            LOGGER.info("finish subscribe channel:{}", join);
                        } finally {
                            //取消订阅
                            try {
                                jedisPubSub.unsubscribe();
                            } catch (Exception e) {
                                //忽略取消订阅的异常
                            }

                            removeSubscribed(jedisPubSub);
                            LOGGER.info("unsubscribe channel:{}", join);
                        }
                    } catch (Exception e) {
                        LOGGER.error("subscribe channel:`" + join + "`,redis group:`" + redisGroup + "` fail", e);
                    }

                    LOGGER.warn("subscribe process exist");

                    if (!stop) {
                        try {
                            LOGGER.warn("subscribe process exist,sleep 1 seconds");
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            ThreadUtil.onInterruptedException(e);
                            return;
                        }
                    } else {
                        return;
                    }
                }
            });
            return true;
        } catch (Exception e) {
            LOGGER.error("subscribe channel:`" + join + "` fail", e);
        }
        return false;
    }

    public String getDeaultRedisGroup() {
        return deaultRedisGroup;
    }

    public String getDefaultChannelName() {
        return defaultChannelName;
    }

    private void addSubscribed(JedisPubSub jedisPubSub) {
        synchronized (this.subscribedLock) {
            if (jedisPubSub == null) {
                return;
            }
            this.subscribed.add(jedisPubSub);
        }
    }

    private void removeSubscribed(JedisPubSub jedisPubSub) {
        synchronized (this.subscribedLock) {
            if (jedisPubSub == null) {
                return;
            }
            this.subscribed.remove(jedisPubSub);
        }
    }

    private synchronized void unsubscribed() {
        synchronized (this.subscribedLock) {
            for (JedisPubSub jedisPubSub : this.subscribed) {
                try {
                    jedisPubSub.unsubscribe();
                } catch (Exception e) {
                    LOGGER.error("unsubscribe error", e);
                }
            }
            this.subscribed.clear();
        }
    }

    @Override
    protected synchronized void execStart() {
        LOGGER.info("start redis subscribe service");
        LOGGER.info("subscribe channel {} at {}", this.defaultChannelName, this.deaultRedisGroup);
        this.subscribe(this.deaultRedisGroup, this.defaultChannelName);
        this.stop = false;
    }

    @Override
    protected synchronized void execStop() {
        LOGGER.info("begin stop redis subscribe service");
        this.stop = true;
        unsubscribed();
        try {
            ThreadUtil.shutdownAndAwaitTermination(this.subscribeExecutor, 3);
        } catch (Exception e) {
            LOGGER.error("stop redis subscribe server error", e);
        }
    }
}

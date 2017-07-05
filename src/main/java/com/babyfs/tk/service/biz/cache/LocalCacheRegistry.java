package com.babyfs.tk.service.biz.cache;

import com.alibaba.fastjson.JSONObject;
import com.babyfs.tk.service.biz.base.model.Message;
import com.babyfs.tk.service.biz.base.model.MessageType;
import com.babyfs.tk.service.biz.constants.Const;
import com.babyfs.tk.service.biz.pubsub.PubSubChannelEvent;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 本地缓存登记,利用{@link Const#EVENTBUS_SUBSCRIBE}监听缓存失效的事件,将本地缓存失效
 */
public class LocalCacheRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalCacheRegistry.class);
    private final ConcurrentMap<Integer, CacheSet> caches = Maps.newConcurrentMap();
    private final MessageType localMessageType = MessageType.LOCAL_CACHE_CHANGE;

    @Inject
    public LocalCacheRegistry(@Named(Const.EVENTBUS_SUBSCRIBE) EventBus eventBus) {
        Preconditions.checkNotNull(eventBus);
        eventBus.register(this);
    }

    /**
     * @param localCacheType 缓存类型,not null
     * @param cache          缓存,not null
     * @return 注册的结果
     */
    public synchronized boolean register(LocalCacheType localCacheType, Cache cache, Function<Object, Object> keyConverter) {
        Preconditions.checkNotNull(localCacheType);
        Preconditions.checkNotNull(cache);

        @SuppressWarnings("unchecked")
        CacheEntry cacheEntry = new CacheEntry(cache, keyConverter);

        CacheSet cacheSet = caches.get(localCacheType.getIndex());
        if (cacheSet == null) {
            CacheSet set = new CacheSet();
            CacheSet preCache = caches.putIfAbsent(localCacheType.getIndex(), set);
            if (preCache != null) {
                cacheSet = preCache;
            } else {
                cacheSet = set;
            }
        }

        return cacheSet.add(cacheEntry);
    }

    /**
     * @param localCacheType 缓存类型,not null
     * @return 取消注册的结果
     */
    public synchronized boolean unregister(LocalCacheType localCacheType) {
        Preconditions.checkNotNull(localCacheType);
        return caches.remove(localCacheType.getIndex()) != null;
    }

    /**
     * @param localCacheType 缓存类型,not null
     * @param cache          not null
     * @return 取消注册的结果
     */
    public synchronized boolean unregister(LocalCacheType localCacheType, Cache cache) {
        Preconditions.checkNotNull(localCacheType);
        Preconditions.checkNotNull(cache);
        CacheSet cacheSet = caches.get(localCacheType.getIndex());
        if (cacheSet == null) {
            return false;
        }
        return cacheSet.remove(cache);
    }

    /**
     * 订阅频道变化事件,过滤出缓存变换的事件,将本地缓存失效
     *
     * @param event
     */
    @Subscribe
    @SuppressWarnings("unchecked")
    public void handleCacheEvent(PubSubChannelEvent event) {
        if (event == null) {
            return;
        }

        String message = event.getMessage();
        if (Strings.isNullOrEmpty(message)) {
            return;
        }

        try {
            Message msg = JSONObject.parseObject(message, Message.class);
            if (msg.getType() != localMessageType.getIndex()) {
                return;
            }

            LocalCacheChangeMessage localCacheChangeMessage = JSONObject.parseObject(message, LocalCacheChangeMessage.class);
            CacheSet cacheSet = this.caches.get(localCacheChangeMessage.getCacheType());
            if (cacheSet == null) {
                return;
            }

            //清理key
            LOGGER.info("Invalidate {},key:{}", localCacheChangeMessage.getCacheType(), localCacheChangeMessage.getKey());
            for (CacheEntry cacheEntry : cacheSet.cacheList) {
                cacheEntry.invalidateKey(localCacheChangeMessage.getKey());
            }
        } catch (Exception e) {
            LOGGER.error("Can't parse event message:`" + message + "`", e);
        }
    }

    private static class CacheSet {
        private final CopyOnWriteArrayList<CacheEntry> cacheList = new CopyOnWriteArrayList();

        public synchronized boolean add(CacheEntry cacheEntry) {
            for (CacheEntry ce : cacheList) {
                if (ce.cache == cacheEntry.cache) {
                    return false;
                }
            }
            cacheList.add(cacheEntry);
            return true;
        }

        public synchronized boolean remove(final Cache cache) {
            return cacheList.removeIf(cacheEntry -> cacheEntry.cache == cache);
        }
    }

    /**
     *
     */
    private static class CacheEntry {
        private final Cache cache;
        private final Function<Object, Object> keyConvert;

        private CacheEntry(Cache cache, Function<Object, Object> keyConvert) {
            this.cache = Preconditions.checkNotNull(cache);
            this.keyConvert = keyConvert;
        }

        void invalidateKey(Object key) {
            if (key == null) {
                return;
            }

            if (keyConvert != null) {
                key = this.keyConvert.apply(key);
            }

            if (key != null) {
                LOGGER.info("invalidate key:{}", key);
                this.cache.invalidate(key);
            }
        }
    }

    public static final Function<Object, Object> ToIntegerFunction = new Function<Object, Object>() {
        @Nullable
        @Override
        public Object apply(@Nullable Object input) {
            if (input == null) {
                return null;
            }

            if (input instanceof Number) {
                return ((Number) input).intValue();
            }
            throw new IllegalArgumentException("wrong type:" + input.getClass());
        }
    };

    public static final Function<Object, Object> ToLongFunction = new Function<Object, Object>() {
        @Nullable
        @Override
        public Object apply(@Nullable Object input) {
            if (input == null) {
                return null;
            }

            if (input instanceof Number) {
                return ((Number) input).longValue();
            }
            throw new IllegalArgumentException("wrong type:" + input.getClass());
        }
    };

    public static final Function<Object, Object> ToStringFunction = new Function<Object, Object>() {
        @Nullable
        @Override
        public Object apply(@Nullable Object input) {
            if (input == null) {
                return null;
            }

            if (input instanceof String) {
                return (String) input;
            }
            throw new IllegalArgumentException("wrong type:" + input.getClass());
        }
    };
}

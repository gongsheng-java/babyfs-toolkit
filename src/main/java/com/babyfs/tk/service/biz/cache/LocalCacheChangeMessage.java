package com.babyfs.tk.service.biz.cache;

import com.alibaba.fastjson.annotation.JSONField;
import com.babyfs.tk.service.biz.base.model.Message;
import com.babyfs.tk.service.biz.base.model.MessageType;

/**
 * 缓存变化消息
 */
public class LocalCacheChangeMessage extends Message {
    /**
     * 缓存的类型
     *
     * @see LocalCacheType#getIndex()
     */
    private int cacheType;
    /**
     * 缓存key
     */
    private Object key;

    @SuppressWarnings("unused")
    public LocalCacheChangeMessage() {
    }


    public LocalCacheChangeMessage(MessageType messageType) {
        super.setType(messageType.getIndex());
    }

    @JSONField(name = "cache_type")
    public int getCacheType() {
        return cacheType;
    }

    @JSONField(name = "cache_type")
    public void setCacheType(int cacheType) {
        this.cacheType = cacheType;
    }

    @JSONField(name = "key")
    public Object getKey() {
        return key;
    }

    @JSONField(name = "key")
    public void setKey(Object key) {
        this.key = key;
    }

    /**
     * @param localCacheType not null
     * @param key            not null
     * @return
     */
    public static LocalCacheChangeMessage newMessage(LocalCacheType localCacheType, Object key) {
        LocalCacheChangeMessage message = new LocalCacheChangeMessage(MessageType.LOCAL_CACHE_CHANGE);
        message.setCacheType(localCacheType.getIndex());
        message.setKey(key);
        return message;
    }
}

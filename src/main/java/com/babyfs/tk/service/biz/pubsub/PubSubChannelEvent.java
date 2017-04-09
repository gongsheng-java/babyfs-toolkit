package com.babyfs.tk.service.biz.pubsub;

import com.babyfs.tk.service.biz.base.model.IEvent;
import com.google.common.base.Preconditions;

/**
 * 订阅的频道事件
 */
public class PubSubChannelEvent implements IEvent {
    private final String channel;
    private final String message;

    /**
     * @param channel 订阅的频道名称,not null
     * @param message 消息,not null
     */
    public PubSubChannelEvent(String channel, String message) {
        this.channel = Preconditions.checkNotNull(channel);
        this.message = Preconditions.checkNotNull(message);
    }

    public String getChannel() {
        return channel;
    }

    public String getMessage() {
        return message;
    }
}

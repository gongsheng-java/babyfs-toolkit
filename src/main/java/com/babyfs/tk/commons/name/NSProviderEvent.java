package com.babyfs.tk.commons.name;

import com.babyfs.tk.commons.event.Event;

import java.util.List;

/**
 * 命名服务提供者的事件,用于通知使用者的服务器变化的事件
 */
public class NSProviderEvent extends Event<NSProviderEventType, List<Server>> {
    public NSProviderEvent(NSProviderEventType nsProviderEventType, List<Server> server) {
        super(nsProviderEventType, server);
    }
}

package com.babyfs.tk.commons.event;

/**
 * 事件监听器接口
 */
public interface IEventListener<T extends Event> {
    public void onEvent(T event);
}

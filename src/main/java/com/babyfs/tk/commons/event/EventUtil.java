package com.babyfs.tk.commons.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public final class EventUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventUtil.class);

    private EventUtil() {

    }

    /**
     * 触发事件
     *
     * @param listenerIterable
     * @param event
     * @param <T>
     */
    public static <T extends Event> void triggerEvent(Iterable<IEventListener<T>> listenerIterable, T event) {
        for (IEventListener<T> listener : listenerIterable) {
            try {
                listener.onEvent(event);
            } catch (Exception e) {
                LOGGER.error("Faile to trigger the listener [" + listener + "]", e);
            }
        }
    }
}

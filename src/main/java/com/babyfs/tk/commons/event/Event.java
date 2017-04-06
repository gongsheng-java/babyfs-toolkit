package com.babyfs.tk.commons.event;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;

/**
 * 通用的事件类型
 *
 * @param <EventType>
 * @param <EventValue>
 */
public class Event<EventType, EventValue> {
    private final EventType type;
    private final EventValue value;

    public Event(@Nonnull EventType type, EventValue value) {
        Preconditions.checkArgument(type != null, "type");
        this.type = type;
        this.value = value;
    }

    public EventType getType() {
        return type;
    }

    public EventValue getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Event event = (Event) o;

        if (type != null ? !type.equals(event.type) : event.type != null) return false;
        if (value != null ? !value.equals(event.value) : event.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}

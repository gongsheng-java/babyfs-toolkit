package com.babyfs.tk.commons.name;

import com.babyfs.tk.commons.event.Event;

import javax.annotation.Nonnull;

/**
 */
public class NSRegisterEvent extends Event<NSRegisterEventType, Void> {
    public NSRegisterEvent(@Nonnull NSRegisterEventType nsRegisterEventType, Void aVoid) {
        super(nsRegisterEventType, aVoid);
    }
}

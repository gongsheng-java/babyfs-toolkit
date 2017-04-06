package com.babyfs.tk.commons.collect;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * 参照{@link java.util.concurrent.CopyOnWriteArrayList} 实现的简单的Copy on write 数组
 */
public class CopyOnWriteArray {
    private final Lock lock = new ReentrantLock();

    private volatile Object[] elements = new Object[0];

    public CopyOnWriteArray() {
    }

    public boolean add(@Nonnull Object element) {
        checkArgument(element != null, "element");
        lock.lock();
        try {
            for (int i = 0; i < elements.length; i++) {
                if (element.equals(elements[i])) {
                    return false;
                }
            }
            final int len = this.elements.length;
            Object[] newElements = Arrays.copyOf(this.elements, len + 1);
            newElements[len] = element;
            this.elements = newElements;
            return true;
        } finally {
            lock.unlock();
        }
    }

    public boolean remove(@Nonnull final  Object element) {
        checkArgument(element != null, "element");
        lock.lock();
        try {
            final int len = this.elements.length;
            if (len == 0) {
                return false;
            }
            final int newLen = len - 1;
            final Object[] newElements = new Object[newLen];
            for (int i = 0; i < len; i++) {
                if (element.equals(this.elements[i])) {
                    for (int k = i + 1; k < len; k++) {
                        newElements[k - 1] = this.elements[k];
                    }
                    this.elements = newElements;
                    return true;
                } else {
                    if (i < newLen) {
                        newElements[i] = this.elements[i];
                    }
                }
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    public Object[] getArray() {
        return this.elements;
    }
}

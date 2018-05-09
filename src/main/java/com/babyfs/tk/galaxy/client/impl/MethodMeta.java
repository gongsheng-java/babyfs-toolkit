package com.babyfs.tk.galaxy.client.impl;

import com.google.common.base.Preconditions;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * 方法元数据
 */
public final class MethodMeta {
    /**
     *
     */
    private final Method method;
    /**
     * 方法签名
     */
    private final String sig;

    /**
     * @param method not null
     * @param sig    not null
     */
    public MethodMeta(Method method, String sig) {
        this.method = Preconditions.checkNotNull(method);
        this.sig = Preconditions.checkNotNull(sig);
    }

    public String getSig() {
        return sig;
    }

    public Method getMethod() {
        return method;
    }
}

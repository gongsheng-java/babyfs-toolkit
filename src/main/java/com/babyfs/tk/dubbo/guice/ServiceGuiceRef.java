package com.babyfs.tk.dubbo.guice;

import com.google.common.base.Preconditions;
import org.apache.commons.lang.StringUtils;

/**
 * Dubbo{@link com.alibaba.dubbo.config.ServiceConfig#ref}引用Guice对象的代理类
 */
public final class ServiceGuiceRef {
    private final String refName;

    public ServiceGuiceRef(String refName) {
        this.refName = Preconditions.checkNotNull(StringUtils.trimToNull(refName), "The refName must not be null or empty");
    }

    public String getRefName() {
        return refName;
    }
}

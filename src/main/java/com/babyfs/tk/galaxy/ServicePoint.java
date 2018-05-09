package com.babyfs.tk.galaxy;

import com.google.common.base.Preconditions;

import java.util.Objects;

/**
 * rpc服务代理目标
 */
public final class ServicePoint<T> {
    /**
     * 服务接口的类型
     */
    private final Class<T> type;
    /**
     * 服务的名称
     */
    private final String name;
    /**
     * 服务接口的完整名称
     */
    private final String interfaceName;

    public ServicePoint(Class<T> type, String name) {
        this.type = Preconditions.checkNotNull(type);
        this.name = name;
        this.interfaceName = Utils.buildServcieName(this.type, this.name);
    }

    public Class<T> getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ServicePoint<?> that = (ServicePoint<?>) o;
        return Objects.equals(type, that.type) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name);
    }
}

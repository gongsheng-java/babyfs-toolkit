package com.babyfs.tk.galaxy.guice;

import com.babyfs.tk.commons.service.ServiceModule;
import com.babyfs.tk.galaxy.constant.RpcConstant;
import com.babyfs.tk.galaxy.ServicePoint;
import com.google.inject.Key;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

/**
 * 提供给RPC服务端暴露服务的服务绑定模块基类
 */
public abstract class RpcServerServiceModule extends ServiceModule {
    protected RpcServerServiceModule() {
    }

    /**
     * 暴露RPC服务接口
     *
     * @param serviceInterface not null
     * @param <T>
     */
    protected <T> void exposeRPCService(final Class<T> serviceInterface) {
        Multibinder<ServicePoint> multibinder = Multibinder.newSetBinder(binder(), ServicePoint.class, Names.named(RpcConstant.NAME_RPC_SERVER_EXPOSE));

        Key<T> targetKey = Key.get(serviceInterface);
        ServicePoint<T> servicePoint = new ServicePoint<>(serviceInterface, null, targetKey);
        multibinder.addBinding().toInstance(servicePoint);
    }

    /**
     * 暴露RPC服务接口
     *
     * @param serviceInterface not null
     * @param name             not null
     * @param <T>
     */
    protected <T> void exposeRPCService(final Class<T> serviceInterface, String name) {
        Multibinder<ServicePoint> multibinder = Multibinder.newSetBinder(binder(), ServicePoint.class, Names.named(RpcConstant.NAME_RPC_SERVER_EXPOSE));

        Key<T> tKey = Key.get(serviceInterface, Names.named(name));
        ServicePoint<T> servicePoint = new ServicePoint<>(serviceInterface, name, tKey);
        multibinder.addBinding().toInstance(servicePoint);
    }
}

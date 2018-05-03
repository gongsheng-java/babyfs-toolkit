package com.babyfs.tk.galaxy.guice;

import com.babyfs.tk.commons.service.ServiceModule;
import com.babyfs.tk.galaxy.constant.RpcConstant;
import com.babyfs.tk.galaxy.ServicePoint;
import com.google.inject.Key;
import com.google.inject.multibindings.MapBinder;
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
        MapBinder<ServicePoint, Object> mapBinder = MapBinder.newMapBinder(binder(), ServicePoint.class, Object.class, Names.named(RpcConstant.NAME_RPC_SERVER_EXPOSE));

        ServicePoint<T> servicePoint = new ServicePoint<T>(serviceInterface, null);
        mapBinder.addBinding(servicePoint).to(Key.get(serviceInterface)).asEagerSingleton();
    }

    /**
     * 暴露RPC服务接口
     *
     * @param serviceInterface not null
     * @param name             not null
     * @param <T>
     */
    protected <T> void exposeRPCService(final Class<T> serviceInterface, String name) {
        MapBinder<ServicePoint, Object> mapBinder = MapBinder.newMapBinder(binder(), ServicePoint.class, Object.class, Names.named(RpcConstant.NAME_RPC_SERVER_EXPOSE));

        ServicePoint<T> servicePoint = new ServicePoint<T>(serviceInterface, name);
        mapBinder.addBinding(servicePoint).to(Key.get(serviceInterface, Names.named(name))).asEagerSingleton();
    }
}

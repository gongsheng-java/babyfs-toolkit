package com.babyfs.tk.galaxy.guice;

import com.babyfs.tk.commons.application.LifeServiceBindUtil;
import com.babyfs.tk.commons.service.LifeServiceSupport;
import com.babyfs.tk.commons.service.ServiceModule;
import com.babyfs.tk.galaxy.constant.RpcConstant;
import com.babyfs.tk.galaxy.server.IServer;
import com.babyfs.tk.galaxy.ServicePoint;
import com.babyfs.tk.galaxy.server.impl.ServerImpl;
import com.google.inject.Key;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Names;

/**
 * RPC Server端的基础module
 */
public class RpcServerSupportModule extends ServiceModule {
    @Override
    protected void configure() {
        MapBinder<ServicePoint, Object> mapBinder = MapBinder.newMapBinder(binder(), ServicePoint.class, Object.class, Names.named(RpcConstant.NAME_RPC_SERVER_EXPOSE));
        bindService(IServer.class, ServerImpl.class);
        LifeServiceBindUtil.addLifeService(binder(), Key.get(IServer.class));
    }

    public static class Init {

    }
}

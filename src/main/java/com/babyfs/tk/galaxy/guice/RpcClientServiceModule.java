package com.babyfs.tk.galaxy.guice;

import com.babyfs.tk.commons.service.ServiceModule;
import com.babyfs.tk.galaxy.client.IClientProxyFactory;
import com.babyfs.tk.galaxy.ServicePoint;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * 提供给RPC客户端使用的服务绑定模块基类
 */
public abstract class RpcClientServiceModule extends ServiceModule {

    protected RpcClientServiceModule() {
    }

    /**
     * 绑定默认的代理服务:服务器自动选择
     *
     * @param serviceInterface
     * @param <T>
     */
    protected <T> void bindRPCService(final Class<T> serviceInterface) {
        Provider<T> provider = new ClientServiceProvider<T>(serviceInterface, null);
        bindService(serviceInterface, provider);
    }

    protected <T> void bindRPCService(final Class<T> serviceInterface, String name) {
        Provider<T> provider = new ClientServiceProvider(serviceInterface, name);
        bindService(serviceInterface, provider, name);
    }

    public static class ClientServiceProvider<T> implements Provider<T> {
        @Inject
        private IClientProxyFactory clientServiceProxy;

        private Class<T> serviceInterface;
        private String name;

        public ClientServiceProvider(Class<T> serviceInterface, String name) {
            this.serviceInterface = serviceInterface;
            this.name = name;
        }

        @Override
        public T get() {
            return clientServiceProxy.newInstance(new ServicePoint<>(serviceInterface, name));
        }
    }
}

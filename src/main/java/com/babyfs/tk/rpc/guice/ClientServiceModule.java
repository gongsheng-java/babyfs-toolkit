package com.babyfs.tk.rpc.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.multibindings.Multibinder;
import com.babyfs.tk.commons.service.ServiceEnrty;
import com.babyfs.tk.rpc.service.ClientProxyStrategy;
import com.babyfs.tk.rpc.service.ClientServiceProxy;

/**
 * 提供给RPC客户端使用的服务绑定模块基类
 */
public abstract class ClientServiceModule extends AbstractModule {

    protected ClientServiceModule() {
    }

    /**
     * 绑定默认的代理服务:服务器自动选择
     *
     * @param serviceInterface
     * @param <T>
     */
    protected <T> void bindService(final Class<T> serviceInterface) {
        bindProxyService(serviceInterface, ClientProxyStrategy.AUTO);
    }


    /**
     * 绑定粘性的代理服务:根据stickyId优先选择server
     *
     * @param serviceInterface
     * @param <T>
     */
    protected <T> void bindStickyService(final Class<T> serviceInterface) {
        bindProxyService(serviceInterface, ClientProxyStrategy.STICKY);
    }

    /**
     * 绑定严格根据serverId选择server的代理服务
     *
     * @param serviceInterface
     * @param <T>
     */
    protected <T> void bindRestrictService(final Class<T> serviceInterface) {
        bindProxyService(serviceInterface, ClientProxyStrategy.RESTRICT);
    }

    /**
     * 绑定指定类型的代理服务
     *
     * @param serviceInterface
     * @param clientProxyStrategy
     * @param <T>
     */
    private <T> void bindProxyService(Class<T> serviceInterface, ClientProxyStrategy clientProxyStrategy) {
        final String serviceName = serviceInterface.getSimpleName();
        Key<T> key = Key.get(serviceInterface);
        bind(key).toProvider(new ClientServiceProvider<T>(serviceName, serviceInterface, clientProxyStrategy)).asEagerSingleton();

        Multibinder<ServiceEnrty> multibinder = Multibinder.newSetBinder(binder(), ServiceEnrty.class);
        multibinder.addBinding().toInstance(new ServiceEnrty(serviceInterface.getSimpleName(), serviceInterface.getName(), key, null));
    }

    public static class ClientServiceProvider<T> implements Provider<T> {
        @Inject
        private ClientServiceProxy clientServiceProxy;
        private String serviceName;
        private Class<T> serviceInterface;
        private ClientProxyStrategy proxyStrategy;

        public ClientServiceProvider(String serviceName, Class<T> serviceInterface, ClientProxyStrategy proxyStrategy) {
            this.serviceName = serviceName;
            this.serviceInterface = serviceInterface;
            this.proxyStrategy = proxyStrategy;
        }

        @Override
        public T get() {
            return clientServiceProxy.buildProxy(serviceName, serviceInterface, proxyStrategy);
        }
    }
}

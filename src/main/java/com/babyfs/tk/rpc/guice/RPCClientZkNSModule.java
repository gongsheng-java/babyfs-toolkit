package com.babyfs.tk.rpc.guice;

import com.google.inject.Inject;
import com.google.inject.PrivateModule;
import com.google.inject.Provider;
import com.babyfs.tk.commons.MapConfig;
import com.babyfs.tk.commons.config.IConfigService;
import com.babyfs.tk.commons.name.INameService;
import com.babyfs.tk.commons.name.INameServiceProvider;
import com.babyfs.tk.commons.name.ServiceRegistry;
import com.babyfs.tk.commons.name.impl.zookeeper.ServerNodeJsonCodec;
import com.babyfs.tk.commons.name.impl.zookeeper.ZkNameServcieProvider;
import com.babyfs.tk.commons.zookeeper.ZkClient;
import com.babyfs.tk.rpc.RPCConfig;

/**
 * RPC客户端基于Zookeepr实现的命名服务查找接口
 */
public class RPCClientZkNSModule extends PrivateModule {
    @Override
    protected void configure() {
        bind(INameService.class).toProvider(NameServiceProvider.class).asEagerSingleton();
        bind(INameServiceProvider.class).toProvider(NameServiceProviderProvider.class).asEagerSingleton();
        expose(INameService.class);
    }

    private static class NameServiceProvider implements Provider<INameService> {
        @Inject
        private INameServiceProvider nameServiceProvider;

        @Override
        public INameService get() {
            return new ServiceRegistry(nameServiceProvider);
        }
    }

    private static class NameServiceProviderProvider implements Provider<INameServiceProvider> {
        @Inject
        private ZkClient zkClient;
        @Inject
        private IConfigService conf;

        @Override
        public INameServiceProvider get() {
            String serviceRootPath = MapConfig.getString(RPCConfig.CONF_RPC_SERVICE_ZK_ROOT, conf, null);
            return new ZkNameServcieProvider(zkClient, serviceRootPath, new ServerNodeJsonCodec());
        }
    }
}

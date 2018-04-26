package com.babyfs.tk.galaxy.guice;

import com.babyfs.tk.commons.MapConfig;
import com.babyfs.tk.commons.config.IConfigService;
import com.babyfs.tk.commons.name.NameConfig;
import com.babyfs.tk.commons.service.IStageActionRegistry;
import com.babyfs.tk.commons.service.annotation.InitStage;
import com.babyfs.tk.galaxy.constant.RpcConstant;
import com.babyfs.tk.galaxy.register.IDiscoveryClient;
import com.babyfs.tk.galaxy.register.ZkDiscoveryClient;
import com.babyfs.tk.galaxy.register.ZkDiscoveryClientBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class ZkDiscoveryClientModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ZkDiscoveryClient.class).toProvider(ZkDiscoveryClientProvide.class).asEagerSingleton();
        requestStaticInjection(StageInit.class);
        bind(IDiscoveryClient.class).toProvider(IDiscoveryClientProvide.class).asEagerSingleton();
    }

    private static class ZkDiscoveryClientProvide implements Provider<ZkDiscoveryClient> {
        @Inject
        private IConfigService conf;

        @Override
        public ZkDiscoveryClient get() {
            ZkDiscoveryClient client = ZkDiscoveryClientBuilder.
                    builder().
                    appName(conf.get(RpcConstant.APP_NAME))
                    .zkRegisterUrl(conf.get(RpcConstant.APP_ZK_SERVERS))
                    .port(MapConfig.getInt(RpcConstant.APP_PORT, conf, RpcConstant.APP_PORT_DEFAULT))
                    .connectTimeout(MapConfig.getInt(RpcConstant.APP_CONNECT_TIMEOUT, conf, RpcConstant.APP_CONNECT_TIMEOUT_DEFAULT))
                    .sessionTimeout(MapConfig.getInt(RpcConstant.APP_SESSION_TIMEOUT, conf, RpcConstant.APP_SESSION_TIMEOUT_DEFAULT))
                    .build();
            return client;
        }
    }

    private static final class StageInit {
        private StageInit() {

        }

        @Inject
        public static void connectToServer(@InitStage IStageActionRegistry registry, final ZkDiscoveryClient zkClient) {
            registry.addAction(new ZkDiscoveryClientModule.StageInit.Connect(zkClient));
        }

        private static class Connect implements Runnable {
            private final ZkDiscoveryClient client;

            public Connect(ZkDiscoveryClient client) {
                this.client = client;
            }

            @Override
            public void run() {
                try {
                    client.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class IDiscoveryClientProvide implements Provider<IDiscoveryClient> {
        @Inject
        private ZkDiscoveryClient client;

        @Override
        public IDiscoveryClient get() {
            return client;
        }
    }


}

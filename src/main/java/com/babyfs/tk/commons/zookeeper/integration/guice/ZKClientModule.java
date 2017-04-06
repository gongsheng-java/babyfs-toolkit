package com.babyfs.tk.commons.zookeeper.integration.guice;

import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.babyfs.tk.commons.config.IConfigService;
import com.babyfs.tk.commons.name.NameConfig;
import com.babyfs.tk.commons.service.IStageActionRegistry;
import com.babyfs.tk.commons.service.annotation.InitStage;
import com.babyfs.tk.commons.service.annotation.ShutdownStage;
import com.babyfs.tk.commons.zookeeper.ZkClient;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 提供基于Zookeeper实现的命名服务
 */
public class ZKClientModule extends AbstractModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZKClientModule.class);

    private final Provider<ZkClient> provider;

    public ZKClientModule() {
        this(null);
    }

    public ZKClientModule(Provider<ZkClient> provider) {
        this.provider = provider;
    }

    @Override
    protected void configure() {
        if (provider == null) {
            bind(ZkClient.class).toProvider(ZkClientProvider.class).asEagerSingleton();
        } else {
            bind(ZkClient.class).toProvider(provider).asEagerSingleton();
        }
        requestStaticInjection(StageInit.class);
    }

    /**
     *
     */
    private static class ZkClientProvider implements Provider<ZkClient> {
        private final Map<String, String> conf;

        @Inject
        public ZkClientProvider(IConfigService conf) {
            this.conf = conf;
        }

        @Override
        public ZkClient get() {
            String zkServers = conf.get(NameConfig.CONF_NAME_ZK_SERVERS);
            String zkUser = conf.get(NameConfig.ZK_AUTH_USER);
            String zkPassword = conf.get(NameConfig.ZK_AUTH_PASSWORD);
            LOGGER.info("zkServers:{}", zkServers);
            return new ZkClient(zkServers, zkUser, zkPassword);
        }
    }

    private static final class StageInit {
        private StageInit() {

        }

        @Inject
        public static void connectToServer(@InitStage IStageActionRegistry registry, final ZkClient zkClient) {
            registry.addAction(new Connect(zkClient));
        }

        @Inject
        public static void close(@ShutdownStage IStageActionRegistry registry, final ZkClient zkClient) {
            registry.addAction(new Close(zkClient));
        }

        private static class Connect implements Runnable {
            private final ZkClient zkClient;

            public Connect(ZkClient zkClient) {
                this.zkClient = zkClient;
            }

            @Override
            public void run() {
                ZooKeeper zooKeeper = zkClient.get();
                Preconditions.checkState(zooKeeper != null, "Can't connect to the Zookeeper servers:%s", zkClient.getZkServers());
            }
        }

        private static class Close implements Runnable {
            private final ZkClient zkClient;

            public Close(ZkClient zkClient) {
                this.zkClient = zkClient;
            }

            @Override
            public void run() {
                zkClient.shutdown();
            }
        }
    }
}

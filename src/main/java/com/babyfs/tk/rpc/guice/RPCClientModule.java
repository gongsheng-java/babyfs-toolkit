package com.babyfs.tk.rpc.guice;

import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.babyfs.tk.commons.MapConfig;
import com.babyfs.tk.commons.config.IConfigService;
import com.babyfs.tk.commons.name.INameService;
import com.babyfs.tk.commons.service.IStageActionRegistry;
import com.babyfs.tk.commons.service.LifecycleModule;
import com.babyfs.tk.commons.service.annotation.InitStage;
import com.babyfs.tk.commons.service.annotation.ShutdownStage;
import com.babyfs.tk.rpc.RPCConfig;
import com.babyfs.tk.rpc.client.ClientHandler;
import com.babyfs.tk.rpc.client.RPCClient;
import com.babyfs.tk.rpc.codec.Codecs;
import com.babyfs.tk.rpc.net.RPCChannelPipelineFactory;
import com.babyfs.tk.rpc.service.ClientServiceProxy;
import org.jboss.netty.channel.ChannelPipelineFactory;

import javax.inject.Provider;

/**
 * 用于创建RPC Client的模块
 */
public class RPCClientModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(RPCClient.class).asEagerSingleton();
        Class<ClientServiceProxyProvider> clientServiceProxyProviderClass = ClientServiceProxyProvider.class;
        bind(ClientServiceProxy.class).toProvider(clientServiceProxyProviderClass).asEagerSingleton();
        requestStaticInjection(StageInit.class);
    }

    public static final class ClientServiceProxyProvider implements Provider<ClientServiceProxy> {
        @Inject(optional = true)
        private IConfigService conf;
        @Inject
        private INameService nameService;
        @Inject
        private RPCClient rpcClient;

        @Override
        public ClientServiceProxy get() {
            byte codecType = MapConfig.getByte(RPCConfig.CONF_RPC_CLIENT_CODEC_TYPE, conf, Codecs.HESSIAN_CODEC.getType());
            long timeout = MapConfig.getLong(RPCConfig.CONF_RPC_CLIENT_TIMEOUT, conf, 1000);
            return new ClientServiceProxy(codecType, nameService, rpcClient, timeout);
        }
    }

    /**
     *
     */
    private static final class StageInit {
        private StageInit() {

        }

        @Inject
        public static void setupClientServiceProxy(@InitStage final IStageActionRegistry registry, final RPCClient rpcClient, ClientServiceProxy clientServiceProxy) {
            registry.addAction(new ClientServiceProxyStart(clientServiceProxy, rpcClient));
        }

        @Inject
        public static void setupShutdown(@ShutdownStage final IStageActionRegistry registry, final RPCClient rpcClient) {
            Preconditions.checkArgument(registry != null, "The @ShutdownStage registry is null,please install the " + LifecycleModule.class.getName());
            registry.addAction(new RPCClientStop(rpcClient));
        }

        private static final class ClientServiceProxyStart implements Runnable {
            private final ClientServiceProxy clientServiceProxy;
            private final RPCClient rpcClient;

            private ClientServiceProxyStart(ClientServiceProxy clientServiceProxy, RPCClient rpcClient) {
                this.clientServiceProxy = clientServiceProxy;
                this.rpcClient = rpcClient;
            }

            @Override
            public void run() {
                rpcClient.addErrorListener(clientServiceProxy);
                ClientHandler clientHandler = new ClientHandler(clientServiceProxy);
                ChannelPipelineFactory channelPipelineFactory = RPCChannelPipelineFactory.makeBinaryPipelineFactory(clientHandler);
                rpcClient.setChannelPipelineFactory(channelPipelineFactory);
                rpcClient.startAsync().awaitRunning();
            }
        }

        private static class RPCClientStop implements Runnable {
            private final RPCClient rpcClient;

            public RPCClientStop(RPCClient rpcClient) {
                this.rpcClient = rpcClient;
            }

            @Override
            public void run() {
                rpcClient.stopAsync().awaitTerminated();
            }
        }
    }
}

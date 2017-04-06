package com.babyfs.tk.rpc.guice;

import com.google.common.base.Preconditions;
import com.google.inject.*;
import com.babyfs.tk.commons.MapConfig;
import com.babyfs.tk.commons.application.IApplication;
import com.babyfs.tk.commons.config.IConfigService;
import com.babyfs.tk.commons.name.INameServiceRegister;
import com.babyfs.tk.commons.service.IContext;
import com.babyfs.tk.commons.service.IStageActionRegistry;
import com.babyfs.tk.commons.service.ServiceEnrty;
import com.babyfs.tk.commons.service.annotation.AfterStartStage;
import com.babyfs.tk.commons.service.annotation.InitStage;
import com.babyfs.tk.commons.service.annotation.ShutdownStage;
import com.babyfs.tk.rpc.RPCConfig;
import com.babyfs.tk.rpc.net.RPCChannelPipelineFactory;
import com.babyfs.tk.rpc.server.RPCServerService;
import com.babyfs.tk.rpc.server.ServerHandler;
import com.babyfs.tk.rpc.service.ServerServiceProxy;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.Executors;

/**
 * 用于创建RPC服务器的模块
 */
public class RPCServerModule extends PrivateModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(RPCServerModule.class);
    /**
     * 默认绑定的地址
     */
    public static final String DEFAULT_IP = "127.0.0.1";
    /**
     * 默认绑定的端口
     */
    public static final int DEFAULT_PORT = 9123;
    /**
     * 默认的线程池个数
     */
    public static final int DEFAULT_POOL_SIZE = 10;

    public RPCServerModule() {
    }

    @Override
    protected void configure() {
        bind(ChannelPipelineFactory.class).toProvider(ChannelPipelineFactoryProvider.class).asEagerSingleton();
        Key<RPCServerService> rpcServerServiceKey = Key.get(RPCServerService.class);
        bind(rpcServerServiceKey).toProvider(RPCServerServiceProvider.class).asEagerSingleton();
        expose(rpcServerServiceKey);

        Key<ServerServiceProxy> serverServiceProxyKey = Key.get(ServerServiceProxy.class);
        ServerServiceProxy serviceProxy = new ServerServiceProxy();
        bind(serverServiceProxyKey).toInstance(serviceProxy);
        expose(serverServiceProxyKey);

        requestStaticInjection(StageInit.class);
    }

    /**
     * RPCServerService的提供者
     */
    private static class RPCServerServiceProvider implements Provider<RPCServerService> {
        @Inject(optional = true)
        private IConfigService conf;

        @Inject
        private ChannelPipelineFactory channelPipelineFactory;

        @Override
        public RPCServerService get() {
            String ip = MapConfig.getString(RPCConfig.CONF_RPC_SERVER_BINDIP, conf, DEFAULT_IP);
            int port = MapConfig.getInt(RPCConfig.CONF_RPC_SERVER_PORT, conf, DEFAULT_PORT);
            LOGGER.info("rpc server will listen at {}:{}", ip, port);
            RPCServerService rpcServerService = new RPCServerService(ip, port);
            rpcServerService.setPipelineFactory(channelPipelineFactory);
            return rpcServerService;
        }
    }

    /**
     *
     */
    private static class ChannelPipelineFactoryProvider implements Provider<ChannelPipelineFactory> {
        @Inject(optional = true)
        private IConfigService conf;

        @Inject
        private ServerServiceProxy serviceProxy;

        @Override
        public ChannelPipelineFactory get() {
            int poolSize = MapConfig.getInt(RPCConfig.CONF_RPC_SERVER_THREADPOOL_SIZE, conf, DEFAULT_POOL_SIZE);
            LOGGER.info("rpc server thread pool size {}", poolSize);
            ServerHandler serverHandler = new ServerHandler(Executors.newFixedThreadPool(poolSize), serviceProxy);
            return RPCChannelPipelineFactory.makeBinaryPipelineFactory(serverHandler);
        }
    }


    /**
     * RPC服务的初始化工作,将所有注册的{@link ServiceEnrty}添加到{@link ServerServiceProxy}代理实例中,接受rpc调用
     */
    private static final class StageInit {
        private StageInit() {

        }

        /**
         * 初始化阶段,将已经注册的服务绑定到RPC Server
         *
         * @param registry
         * @param allServices
         * @param serviceProxy
         * @param context
         */
        @Inject
        public static void buildServiceProxy(@InitStage final IStageActionRegistry registry, final Set<ServiceEnrty> allServices, final ServerServiceProxy serviceProxy, final IContext context) {
            registry.addAction(new BindServiceProxyRunnable(context, allServices, serviceProxy));
        }

        @Inject(optional = true)
        public static void addToApplication(final IApplication application, final RPCServerService rpcServerService) {
            if (application != null) {
                application.addILifeServvice(rpcServerService);
            }
        }

        /**
         * 服务启动后注册servcie到命名服务
         *
         * @param registry
         * @param nameServiceRegister
         * @param allServices
         */
        @Inject(optional = true)
        public static void registerToNameServcie(@AfterStartStage final IStageActionRegistry registry, @ShutdownStage final IStageActionRegistry shutdowAction, final INameServiceRegister nameServiceRegister, final Set<ServiceEnrty> allServices) {
            if (nameServiceRegister != null) {
                registry.addAction(new RegisteToNameServcie(allServices, nameServiceRegister));
                shutdowAction.addAction(new UnRegister(nameServiceRegister));
            }
        }

        private static class BindServiceProxyRunnable implements Runnable {
            private final IContext context;
            private final Set<ServiceEnrty> allServices;
            private final ServerServiceProxy serviceProxy;

            public BindServiceProxyRunnable(IContext context, Set<ServiceEnrty> allServices, ServerServiceProxy serviceProxy) {
                this.context = context;
                this.allServices = allServices;
                this.serviceProxy = serviceProxy;
            }

            @Override
            @SuppressWarnings({"deprecation"})
            public void run() {
                LOGGER.info("Exec start stage,add all service to service proxy");
                Injector injector = context.getInjector();
                Preconditions.checkNotNull(injector, "The injector must be set in " + context);
                for (ServiceEnrty module : allServices) {
                    String serviceName = module.getName();
                    Key<?> serviceKey = module.getGuiceKey();
                    Object instance = injector.getInstance(serviceKey);
                    serviceProxy.add(serviceName, instance);
                    LOGGER.info("Register service " + serviceName + "=" + instance);
                }
            }
        }

        private static class RegisteToNameServcie implements Runnable {
            private final Set<ServiceEnrty> allServices;
            private final INameServiceRegister nameServiceRegister;

            public RegisteToNameServcie(Set<ServiceEnrty> allServices, INameServiceRegister nameServiceRegister) {
                this.allServices = allServices;
                this.nameServiceRegister = nameServiceRegister;
            }

            @Override
            public void run() {
                LOGGER.info("Begin register all servcies to NameServcieRegister{}.", nameServiceRegister);
                for (ServiceEnrty serviceEnrty : allServices) {
                    nameServiceRegister.addService(serviceEnrty.getName());
                }
                boolean success = nameServiceRegister.register();
                LOGGER.info("Finish register all servcies to NameServcieRegister{},result:{}", nameServiceRegister, success);
                if (!success) {
                    throw new RuntimeException("Registe rpc services to register " + nameServiceRegister + " fail");
                }
            }
        }

        private static class UnRegister implements Runnable {
            private final INameServiceRegister nameServiceRegister;

            public UnRegister(INameServiceRegister nameServiceRegister) {
                this.nameServiceRegister = nameServiceRegister;
            }

            @Override
            public void run() {
                nameServiceRegister.unRegister();
            }
        }
    }
}

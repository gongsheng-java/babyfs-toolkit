package com.babyfs.tk.rpc.guice;

import com.google.inject.Inject;
import com.google.inject.PrivateModule;
import com.google.inject.Provider;
import com.babyfs.tk.commons.Constants;
import com.babyfs.tk.commons.MapConfig;
import com.babyfs.tk.commons.config.IConfigService;
import com.babyfs.tk.commons.name.INameServiceRegister;
import com.babyfs.tk.commons.name.impl.zookeeper.ServerNodeJsonCodec;
import com.babyfs.tk.commons.name.impl.zookeeper.ZkNameServiceRegister;
import com.babyfs.tk.commons.zookeeper.ZkClient;
import com.babyfs.tk.rpc.RPCConfig;

/**
 * RPC服务基于Zookeeper实现的命名服务注册Module
 */
public class RPCServerZkNSRegModule extends PrivateModule {
    @Override
    protected void configure() {
        bind(INameServiceRegister.class).toProvider(INameServiceRegisterProvider.class).asEagerSingleton();
        expose(INameServiceRegister.class);
    }

    private static class INameServiceRegisterProvider implements Provider<INameServiceRegister> {
        @Inject()
        private IConfigService conf;
        @Inject
        private ZkClient zkClient;

        @Override
        public INameServiceRegister get() {
            String serverId = MapConfig.getString(Constants.CONF_SERVER_ID, conf, "Unknown");
            String serviceRootPath = MapConfig.getString(RPCConfig.CONF_RPC_SERVICE_ZK_ROOT, conf, null);
            String bindIp = MapConfig.getString(RPCConfig.CONF_RPC_SERVER_BINDIP, conf, null);
            int port = MapConfig.getInt(RPCConfig.CONF_RPC_SERVER_PORT, conf, 0);
            return new ZkNameServiceRegister(zkClient, serverId, bindIp, port, serviceRootPath,new ServerNodeJsonCodec());
        }
    }
}

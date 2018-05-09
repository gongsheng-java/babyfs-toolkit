package com.babyfs.tk.galaxy.guice;

import com.babyfs.tk.commons.MapConfig;
import com.babyfs.tk.commons.application.LifeServiceBindUtil;
import com.babyfs.tk.commons.config.IConfigService;
import com.babyfs.tk.commons.service.ServiceModule;
import com.babyfs.tk.galaxy.Utils;
import com.babyfs.tk.galaxy.constant.RpcConstant;
import com.babyfs.tk.galaxy.register.IServcieRegister;
import com.babyfs.tk.galaxy.register.impl.ZkServiceRegister;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.Provider;
import org.apache.curator.framework.CuratorFramework;

public class ZkServiceRegisterModule extends ServiceModule {

    @Override
    protected void configure() {
        bindServiceWithProvider(IServcieRegister.class, ZkServcieRegisterProvider.class);
        LifeServiceBindUtil.addLifeService(binder(), Key.get(IServcieRegister.class));
    }

    private static class ZkServcieRegisterProvider implements Provider<ZkServiceRegister> {
        @Inject
        private IConfigService conf;

        @Override
        public ZkServiceRegister get() {
            String zkRegisterUrl = MapConfig.getString(RpcConstant.ZK_BOOTSTRAP_SERVERS, conf, RpcConstant.ZK_BOOTSTRAP_SERVERS_DEFAULT);
            String ip = MapConfig.getString(RpcConstant.SERVER_IP, conf, "127.0.0.1");
            int port = MapConfig.getInt(RpcConstant.SERVER_PORT, conf, 0);
            Preconditions.checkState(port > 0, "port");
            int connectTimeout = MapConfig.getInt(RpcConstant.ZK_CONNECT_TIMEOUT, conf, RpcConstant.ZK_CONNECT_TIMEOUT_DEFAULT);
            int sessionTimeout = MapConfig.getInt(RpcConstant.ZK_SESSION_TIMEOUT, conf, RpcConstant.ZK_SESSION_TIMEOUT_DEFAULT);
            String serverRoot = MapConfig.getString(RpcConstant.ZK_REGISTER_ROOT, conf, RpcConstant.ZK_REGISTER_ROOT_DEFAULT);
            CuratorFramework curator = Utils.buildAndStartCurator(zkRegisterUrl, connectTimeout, sessionTimeout);
            return new ZkServiceRegister(curator, ip, port, serverRoot);
        }
    }
}

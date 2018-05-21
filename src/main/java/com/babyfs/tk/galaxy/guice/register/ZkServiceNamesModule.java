package com.babyfs.tk.galaxy.guice.register;

import com.babyfs.tk.commons.MapConfig;
import com.babyfs.tk.commons.application.LifeServiceBindUtil;
import com.babyfs.tk.commons.config.IConfigService;
import com.babyfs.tk.commons.service.ServiceModule;
import com.babyfs.tk.galaxy.Utils;
import com.babyfs.tk.galaxy.constant.RpcConstant;
import com.babyfs.tk.galaxy.register.IServiceNames;
import com.babyfs.tk.galaxy.register.impl.ZkServiceNames;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.Provider;
import org.apache.curator.framework.CuratorFramework;

import static com.google.common.base.Preconditions.checkState;

/**
 * {@link IServiceNames}的Zk实现
 */
public class ZkServiceNamesModule extends ServiceModule {

    @Override
    protected void configure() {
        bindServiceWithProvider(IServiceNames.class, ZkServcieNamesProvider.class);
        LifeServiceBindUtil.addLifeService(binder(), Key.get(IServiceNames.class));
    }

    private static class ZkServcieNamesProvider implements Provider<ZkServiceNames> {
        @Inject
        private IConfigService conf;

        @Override
        public ZkServiceNames get() {
            String zkRegisterUrl = MapConfig.getString(RpcConstant.ZK_BOOTSTRAP_SERVERS, conf, RpcConstant.ZK_BOOTSTRAP_SERVERS_DEFAULT);
            int connectTimeout = MapConfig.getInt(RpcConstant.ZK_CONNECT_TIMEOUT, conf, RpcConstant.ZK_CONNECT_TIMEOUT_DEFAULT);
            int sessionTimeout = MapConfig.getInt(RpcConstant.ZK_SESSION_TIMEOUT, conf, RpcConstant.ZK_SESSION_TIMEOUT_DEFAULT);
            String serverRoot = MapConfig.getString(RpcConstant.ZK_REGISTER_ROOT, conf, RpcConstant.ZK_REGISTER_ROOT_DEFAULT);
            return build(zkRegisterUrl, serverRoot, connectTimeout, sessionTimeout);
        }

        public static ZkServiceNames build(String zkRegisterUrl, String serverRoot, int connectTimeout, int sessionTimeout) {
            checkState(!Strings.isNullOrEmpty(serverRoot), "serverRoot");

            CuratorFramework curator = Utils.buildAndStartCurator(zkRegisterUrl, connectTimeout, sessionTimeout);
            return new ZkServiceNames(curator, serverRoot);
        }
    }

}

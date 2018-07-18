package com.babyfs.tk.service.biz.serialnum.guice;

import com.babyfs.tk.commons.MapConfig;
import com.babyfs.tk.commons.application.LifeServiceBindUtil;
import com.babyfs.tk.commons.config.IConfigService;
import com.babyfs.tk.service.biz.serialnum.ISerialNumServiceRegister;
import com.babyfs.tk.service.biz.serialnum.consts.SerialNumConstant;
import com.babyfs.tk.service.biz.serialnum.impl.ZkSerialNumServiceRegister;
import com.babyfs.tk.commons.service.ServiceModule;
import com.babyfs.tk.galaxy.constant.RpcConstant;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.Provider;
import org.apache.curator.framework.CuratorFramework;

import static com.babyfs.tk.galaxy.Utils.buildAndStartCurator;

public class ZkSNServiceRegisterModule extends ServiceModule {
    @Override
    protected void configure() {
        bindServiceWithProvider(ISerialNumServiceRegister.class, ZkSNServiceRegisterModule.ZkSNServiceRegisterProvider.class);
        LifeServiceBindUtil.addLifeService(binder(), Key.get(ISerialNumServiceRegister.class));
    }

    private static class ZkSNServiceRegisterProvider implements Provider<ZkSerialNumServiceRegister> {
        @Inject
        private IConfigService conf;

        @Override
        public ZkSerialNumServiceRegister get() {
            String zkRegisterUrl = MapConfig.getString(SerialNumConstant.ZK_BOOTSTRAP_SERVERS, conf, SerialNumConstant.ZK_BOOTSTRAP_SERVERS_DEFAULT);
            int connectTimeout = MapConfig.getInt(SerialNumConstant.ZK_CONNECT_TIMEOUT, conf, SerialNumConstant.ZK_CONNECT_TIMEOUT_DEFAULT);
            int sessionTimeout = MapConfig.getInt(SerialNumConstant.ZK_SESSION_TIMEOUT, conf, SerialNumConstant.ZK_SESSION_TIMEOUT_DEFAULT);
            String zkRoot = MapConfig.getString(SerialNumConstant.ZK_SERIAL_NUMBER_ROOT, conf, SerialNumConstant.ZK_SERIAL_NUMBER_ROOT_DEFAULT);
            String zkNode = MapConfig.getString(SerialNumConstant.ZK_SERIAL_NUMBER_NODE, conf, SerialNumConstant.ZK_SERIAL_NUMBER_NODE_DEFAULT);
            CuratorFramework curator = buildAndStartCurator(zkRegisterUrl, connectTimeout, sessionTimeout);
            return new ZkSerialNumServiceRegister(zkRoot, zkNode, curator);
        }
    }
}

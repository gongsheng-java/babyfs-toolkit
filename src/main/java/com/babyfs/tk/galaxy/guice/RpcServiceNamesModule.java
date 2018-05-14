package com.babyfs.tk.galaxy.guice;

import com.babyfs.tk.commons.service.ServiceModule;
import com.babyfs.tk.galaxy.constant.RpcConstant;
import com.babyfs.tk.galaxy.register.IServiceNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 根据{@link RpcConstant#REGISTER_MODE}选择{@link IServiceNames}的实现
 */
public class RpcServiceNamesModule extends ServiceModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServiceNamesModule.class);

    @Override
    protected void configure() {
        String mode = System.getProperty(RpcConstant.REGISTER_MODE, RpcConstant.REGISTER_MODE_LOCAL);
        LOGGER.info("rpc register mode:{}", mode);
        if (RpcConstant.REGISTER_MODE_LOCAL.equals(mode)) {
            install(new LocalServiceNamesModule());
        } else if (RpcConstant.REGISTER_MODE_ZK.equals(mode)) {
            install(new ZkServiceNamesModule());
        } else {
            throw new IllegalArgumentException("unsupport rpc register mode:" + mode);
        }
    }
}

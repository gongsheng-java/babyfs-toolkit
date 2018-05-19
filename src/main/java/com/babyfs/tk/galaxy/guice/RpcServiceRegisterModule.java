package com.babyfs.tk.galaxy.guice;

import com.babyfs.tk.commons.service.ServiceModule;
import com.babyfs.tk.galaxy.constant.RpcConstant;
import com.babyfs.tk.galaxy.guice.register.LocalServiceRegisterModule;
import com.babyfs.tk.galaxy.guice.register.ZkServiceRegisterModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 根据{@link RpcConstant#REGISTER_MODE}选择{@link com.babyfs.tk.galaxy.register.IServcieRegister}的实现
 */
public class RpcServiceRegisterModule extends ServiceModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServiceRegisterModule.class);

    @Override
    protected void configure() {
        String model = System.getProperty(RpcConstant.REGISTER_MODE, RpcConstant.REGISTER_MODE_LOCAL);
        LOGGER.info("rpc register mode:{}", model);

        if (RpcConstant.REGISTER_MODE_LOCAL.equals(model)) {
            install(new LocalServiceRegisterModule());
        } else if (RpcConstant.REGISTER_MODE_ZK.equals(model)) {
            install(new ZkServiceRegisterModule());
        } else {
            throw new IllegalArgumentException("unsupport rpc register mode:" + model);
        }
    }
}

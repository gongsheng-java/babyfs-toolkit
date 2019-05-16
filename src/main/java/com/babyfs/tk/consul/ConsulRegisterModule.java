package com.babyfs.tk.consul;

import com.babyfs.servicetk.grpcapicore.registry.ServiceRegister;
import com.babyfs.tk.apollo.ApolloUtil;
import com.babyfs.tk.apollo.ConfigLoader;
import com.babyfs.tk.commons.MapConfig;
import com.babyfs.tk.commons.config.IConfigService;
import com.babyfs.tk.commons.service.ServiceModule;
import com.babyfs.tk.galaxy.constant.RpcConstant;
import com.babyfs.tk.galaxy.register.impl.ZkServiceRegister;
import com.google.inject.Inject;
import org.elasticsearch.common.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.babyfs.servicetk.grpcapicore.EnvConstant.APOLLO_GRPC_NAMESPACE;
import static com.babyfs.servicetk.grpcapicore.EnvConstant.KEY_CONSUL_REGISTRY_ADDRESS;
import static com.babyfs.tk.galaxy.constant.RpcConstant.SERVER_NAME;

public class ConsulRegisterModule  extends ServiceModule {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZkServiceRegister.class);
    @Inject
    private IConfigService conf;

    @Override
    protected void configure() {
        //临时加在这
        String port = System.getProperty(RpcConstant.SERVER_PORT);
        if(port == null){
            LOGGER.info("no input server_port as jvm_option, try to get from apollo");
            port = System.getProperty(RpcConstant.SERVER_PORT);
        }
        int portNum = 0;
        if(Strings.isNullOrEmpty(port))
        {
            LOGGER.info("no input server_port as jvm_option or apollo, register service to consul for port 0");
        }else{
            try{
                portNum = Integer.parseInt(port);
            }catch (Exception e){
                LOGGER.info("error no input server_port {} as jvm_option, can't register service to consul", port);
                return;
            }
        }

        String serverName = System.getProperty(SERVER_NAME);
        if(serverName == null){
            serverName = ConfigLoader.getConfig(SERVER_NAME);
            if(Strings.isNullOrEmpty(serverName)){
                serverName = ApolloUtil.getAppId();
                LOGGER.info("no input server_name as jvm_option or apollo, register service as appid to consul");

            }
        }

        LOGGER.info("register service to consul");
        String registry = ConfigLoader.getConfig(APOLLO_GRPC_NAMESPACE, KEY_CONSUL_REGISTRY_ADDRESS);
        ServiceRegister.init(serverName, portNum, registry);
        ServiceRegister.defaultRegister();
    }
}

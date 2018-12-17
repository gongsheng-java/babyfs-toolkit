package com.babyfs.tk.grpc.guice;

import com.babyfs.servicetk.grpcapicore.ProxyBuilder;
import com.babyfs.tk.apollo.ConfigLoader;
import com.babyfs.tk.apollo.guice.ApolloModule;
import com.babyfs.tk.commons.service.ServiceModule;
import org.elasticsearch.common.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrpcInitModule  extends ServiceModule {

    private static final String GRPC_INTERFACES_NAMESPACE = "babyfs.grpc";
    private static final String GRPC_INTERFACES_KEY = "guice.interfaces";

    private final static Logger logger = LoggerFactory.getLogger(ApolloModule.class);


    public GrpcInitModule(){

    }

    @Override
    protected void configure() {
        String config = ConfigLoader.getConfig(GRPC_INTERFACES_NAMESPACE, GRPC_INTERFACES_KEY);
        if(Strings.isNullOrEmpty(config)){
            return;
        }

        String[] interfaces = config.split(",");
        for (String grpcInter :
                interfaces) {
            buildModule(grpcInter);
        }
    }

    private void buildModule(String inter){
        Class tClass = null;
        try {
            tClass = Class.forName(inter);
        } catch (Exception e) {
            logger.warn("find class {} fail, cannot create grpc client", inter);
            return;
        }
        try {
            bindService(tClass, ProxyBuilder.buildProxy(tClass));
            logger.info("bind grpc service {} succeed", inter);
        }catch (Exception e){
            logger.error("bind grpc failed, {} has already been registered", tClass.getName());
        }


    }
}

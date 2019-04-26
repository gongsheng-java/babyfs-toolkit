package com.babyfs.tk.grpc.guice;

import com.babyfs.servicetk.grpcapicore.ProxyBuilder;
import com.babyfs.tk.apollo.guice.ApolloModule;
import com.babyfs.tk.commons.service.ServiceModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author kata
 */
public abstract class GrpcAbstractModule extends ServiceModule {

    protected abstract Class[] grpcClients();
    private final static Logger logger = LoggerFactory.getLogger(ApolloModule.class);

    @Override
    protected void configure() {

        for (Class grpcInter :
                grpcClients()) {
            buildModule(grpcInter);
        }
    }

    private void buildModule(Class grpcInter){
        try {
            bindService(grpcInter, ProxyBuilder.buildProxy(grpcInter));
            logger.info("bind grpc service {} succeed", grpcInter);
        }catch (Exception e){
            logger.error("bind {}  grpc failed", grpcInter.getName());
            logger.error("failed resean:{}", e);
        }
    }
}

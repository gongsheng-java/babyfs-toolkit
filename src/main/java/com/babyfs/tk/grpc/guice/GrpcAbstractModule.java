package com.babyfs.tk.grpc.guice;

import com.babyfs.servicetk.grpcapicore.ProxyBuilder;
import com.babyfs.tk.apollo.guice.ApolloModule;
import com.babyfs.tk.commons.service.ServiceModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kata
 */
public abstract class GrpcAbstractModule extends ServiceModule {

    private static ConcurrentHashMap<Class, Object> REGISTER_CONTAINER = new ConcurrentHashMap<>();

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
        REGISTER_CONTAINER.computeIfAbsent(grpcInter, k -> {
            Object result = null;
            try {
                result = ProxyBuilder.buildProxy(grpcInter);
                bindService(grpcInter, result);
                logger.info("bind grpc service {} succeed", grpcInter);
            }catch (Exception e){
                logger.error("bind {}  grpc failed", grpcInter.getName());
                logger.error("failed resean:{}", e);
            }
            return result;
        });

    }
}

package com.babyfs.tk.commons.service.internal;

import com.babyfs.tk.commons.service.ILifeService;
import com.babyfs.tk.commons.service.annotation.ShutdownStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ShutdownStage}
 */
public class ShutdownStageRegistry extends StageActionRegistrySupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShutdownStageRegistry.class);

    @Override
    public synchronized void execute() {
        LOGGER.info("disable start life servcie:{}", isDisableStartLifeServcie());

        if (this.lifeServices != null && !isDisableStartLifeServcie()) {
            LOGGER.info("Stoping life services");

            //按Order降序序依次停止服务
            for (ILifeService lifeService : sortByOrderDesc(this.lifeServices)) {
                LOGGER.info("Stoping service name:{},class:{}", lifeService.getName(), lifeService.getClass());
                lifeService.stopAsync().awaitTerminated();
                LOGGER.info("Finish stopping service name:{},class:{}", lifeService.getName(), lifeService.getClass());
            }
            LOGGER.info("Stopping life service,finish");
        }
        super.execute();
    }

    @Override
    public String toString() {
        return ShutdownStage.class.toString();
    }
}

package com.babyfs.tk.commons.service.internal;

import com.babyfs.tk.commons.enums.ShutdownOrder;
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
        if (this.lifeServices != null && !isDisableStartLifeServcie()) {
            //按Order降序序依次停止服务
            for (ILifeService lifeService : sortByOrderAsc(this.lifeServices, ShutdownOrder.class)) {
                LOGGER.info("stop {}", lifeService.getDescription());
                lifeService.stopAsync().awaitTerminated();
                LOGGER.info("{} stopped", lifeService.getDescription());
            }
        }
        super.execute();
    }

    @Override
    public String toString() {
        return ShutdownStage.class.toString();
    }
}

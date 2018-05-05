package com.babyfs.tk.commons.service.internal;

import com.babyfs.tk.commons.service.ILifeService;
import com.babyfs.tk.commons.service.annotation.InitStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;

/**
 * {@link InitStage}
 */
public class InitStageRegistry extends StageActionRegistrySupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(InitStageRegistry.class);

    @Override
    public synchronized void execute() {
        super.execute();
        if (this.lifeServices != null && !isDisableStartLifeServcie()) {
            //按Order升序依次启动服务
            for (ILifeService lifeService : sortByOrderAsc(this.lifeServices, Order.class)) {
                LOGGER.info("start {}", lifeService.getDescription());
                lifeService.startAsync().awaitRunning();
                LOGGER.info("{} started", lifeService.getDescription());
            }
        }
    }

    @Override
    public String toString() {
        return InitStage.class.toString();
    }
}

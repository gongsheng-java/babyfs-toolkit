package com.babyfs.tk.commons.service.internal;

import com.babyfs.tk.commons.service.ILifeService;
import com.babyfs.tk.commons.service.annotation.InitStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link InitStage}
 */
public class InitStageRegistry extends StageActionRegistrySupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(InitStageRegistry.class);

    @Override
    public synchronized void execute() {
        super.execute();
        LOGGER.info("disable start life servcie:{}", isDisableStartLifeServcie());
        if (this.lifeServices != null && !isDisableStartLifeServcie()) {
            LOGGER.info("Starting life services");

            //按Order升序依次启动服务
            for (ILifeService lifeService : sortByOrderAsc(this.lifeServices)) {
                LOGGER.info("Starting service name:{},class:{}", lifeService.getName(), lifeService.getClass());
                lifeService.startAsync().awaitRunning();
                LOGGER.info("Finish starting service name:{},class:{}", lifeService.getName(), lifeService.getClass());
            }
            LOGGER.info("Starting life service,finish");
        }
    }

    @Override
    public String toString() {
        return InitStage.class.toString();
    }
}

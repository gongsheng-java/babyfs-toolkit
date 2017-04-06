package com.babyfs.tk.dubbo.guice;

import com.alibaba.dubbo.config.ServiceConfig;
import com.google.inject.Inject;
import com.babyfs.tk.commons.service.IStageActionRegistry;
import com.babyfs.tk.commons.service.annotation.InitStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Dubbo服务导出工具类
 */
public class DubboServiceExport {
    private static final Logger LOGGER = LoggerFactory.getLogger(DubboServiceExport.class);
    private final List<ServiceConfig> serviceConfigs;

    public DubboServiceExport(List<ServiceConfig> serviceConfigs) {
        this.serviceConfigs = serviceConfigs;
    }

    @Inject
    public void setUpExpose(@InitStage IStageActionRegistry initStageActionRegistry) {
        initStageActionRegistry.addAction(new Runnable() {
            @Override
            public void run() {
                if (serviceConfigs == null || serviceConfigs.isEmpty()) {
                    LOGGER.warn("No ServiceConfig found");
                    return;
                }
                for (ServiceConfig serviceConfig : serviceConfigs) {
                    LOGGER.info("Expose service {}", serviceConfig);
                    serviceConfig.export();
                }
            }
        });
    }
}

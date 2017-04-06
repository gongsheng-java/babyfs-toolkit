package com.babyfs.tk.service.basic.probe.guice;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.PrivateModule;
import com.babyfs.tk.service.basic.probe.Config;
import com.babyfs.tk.service.basic.probe.IProbeSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 基于Logger的Probe Module,这是一个私有的Module,不暴露任何key,只用来初始化ProbeService
 */
public class LoggerProbeModule extends PrivateModule {
    private static final Logger LOGGER = LoggerFactory.getLogger("porbe");
    /**
     * Probe的配置
     */
    private final Config config;

    /**
     * @param config
     */
    public LoggerProbeModule(Config config) {
        this.config = config;
    }

    @Override
    protected void configure() {
        if (this.config != null) {
            bind(Config.class).toInstance(this.config);
        }
        bind(IProbeSender.class).to(LoggerProbeSender.class).asEagerSingleton();
        expose(IProbeSender.class);
    }

    public static class LoggerProbeSender implements IProbeSender {
        @Override
        public void send(String probeName, String message) {
            Preconditions.checkNotNull(probeName);
            if (Strings.isNullOrEmpty(message)) {
                return;
            }
            LOGGER.info("probeName:{},data:{}", probeName, message);
        }
    }
}

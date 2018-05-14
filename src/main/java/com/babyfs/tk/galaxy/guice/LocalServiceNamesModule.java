package com.babyfs.tk.galaxy.guice;

import com.babyfs.tk.commons.MapConfig;
import com.babyfs.tk.commons.config.IConfigService;
import com.babyfs.tk.commons.service.ServiceModule;
import com.babyfs.tk.galaxy.constant.RpcConstant;
import com.babyfs.tk.galaxy.register.IServiceNames;
import com.babyfs.tk.galaxy.register.ServiceServer;
import com.babyfs.tk.galaxy.register.impl.LocalServiceNames;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * {@link IServiceNames}的本地实现
 */
public class LocalServiceNamesModule extends ServiceModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalServiceNamesModule.class);


    @Override
    protected void configure() {
        bindServiceWithProvider(IServiceNames.class, LocalServcieNamesProvider.class);
    }

    private static class LocalServcieNamesProvider implements Provider<LocalServiceNames> {
        @Inject
        private IConfigService conf;

        @Override
        public LocalServiceNames get() {
            String servers = MapConfig.getString(RpcConstant.REGISTER_LOCAL_SERVERS, conf, "");

            List<ServiceServer> serverList = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(servers).stream().map(input -> {
                List<String> strings = Splitter.on(":").trimResults().omitEmptyStrings().splitToList(input);
                Preconditions.checkArgument(strings.size() == 2);
                return new ServiceServer("", strings.get(0), Integer.parseInt(strings.get(1)));
            }).collect(Collectors.toList());


            LOGGER.info("rpc servers:{}",serverList);
            return new LocalServiceNames(serverList);
        }
    }
}

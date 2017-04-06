package com.babyfs.tk.service.basic.security.guice;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.babyfs.tk.service.basic.guice.BasicServiceModule;
import com.babyfs.tk.service.basic.security.ICryptoService;

/**
 * {@link ICryptoService}的默认Guice Module
 *
 * @see {@link CryptoServiceProvider}
 */
public class CryptoServiceModule extends BasicServiceModule {
    public static final String CONF = "crypto.xml";

    private final String conf;

    /**
     *
     */
    public CryptoServiceModule() {
        this(CONF);
    }

    /**
     * @param conf {@link CryptoServiceProvider}需要的配置文件名称
     */
    public CryptoServiceModule(String conf) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(conf), "The conf must not be null or empty");
        this.conf = conf;
    }


    @Override
    protected void configure() {
        bindServiceWithConf(ICryptoService.class, conf, CryptoServiceProvider.class);
    }
}

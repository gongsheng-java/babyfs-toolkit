package com.babyfs.tk.service.biz.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ContextLoaderListener;

import javax.servlet.ServletContextEvent;

/**
 * 快速失败的ContextLoaderListener,如果初始化失败,则退出JVM
 */
public class FastFailSpringContenxtLoaderListener extends ContextLoaderListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(FastFailSpringContenxtLoaderListener.class);

    @Override
    public void contextInitialized(ServletContextEvent event) {
        try {
            super.contextInitialized(event);
        } catch (Throwable e) {
            LOGGER.error("Context init fail,stop jvm", e);
            System.exit(1);
        }
    }
}

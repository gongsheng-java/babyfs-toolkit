package com.babyfs.tk.service.biz.web;


import com.babyfs.tk.commons.service.GuiceInjector;
import com.babyfs.tk.commons.service.IContext;
import com.babyfs.tk.commons.service.IStageActionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletException;

/**
 * 当Spring Servlet初始完成后,执行一些Guice相关的操作
 */
public class GuiceDispatcherServlet extends DispatcherServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(GuiceDispatcherServlet.class);

    @Override
    protected void initFrameworkServlet() throws ServletException {
        super.initFrameworkServlet();
        WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
        GuiceInjector guiceInjector = (GuiceInjector) webApplicationContext.getBean(GuiceInjector.GUICE_INJECTOR_BEAN_NAME);
        final IStageActionRegistry afterActionRegistry = guiceInjector.getInjector().getInstance(IContext.class).getAfterActionRegistry();
        if (afterActionRegistry != null) {
            LOGGER.info("Execute after " + afterActionRegistry.getClass());
            afterActionRegistry.execute();
        }
    }
}

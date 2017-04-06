package com.babyfs.tk.http.upload;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Injector;
import com.babyfs.tk.commons.config.IConfigService;
import com.babyfs.tk.commons.service.GuiceInjector;
import com.babyfs.tk.commons.utils.ListUtil;
import com.babyfs.tk.http.upload.config.Config2ServletEntryFunc;
import com.babyfs.tk.http.upload.config.ServletConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;
import java.util.List;

/**
 * Servlet 3.0 Multipart Servlet初始化
 */
public final class MultiPartServletInitListener implements ServletContextListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(MultiPartServletInitListener.class);
    /**
     * Context配置名,用于指定初始化的配置文件名,所指定的配置文件名应该在ClassPath中可以找到
     */
    public static final String MULTIPART_CONFIG_PARAM = "multipartConfig";

    public static final String CONF_SERVLETS = "servlets";

    @Override
    public synchronized void contextInitialized(ServletContextEvent sce) {
        WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(sce.getServletContext());
        GuiceInjector guiceInjector = (GuiceInjector) webApplicationContext.getBean(GuiceInjector.GUICE_INJECTOR_BEAN_NAME);
        Preconditions.checkNotNull(guiceInjector, "Can't find the Guice Injector by name %s", GuiceInjector.GUICE_INJECTOR_BEAN_NAME);
        IConfigService configService = guiceInjector.getInjector().getInstance(IConfigService.class);

        ServletContext servletContext = sce.getServletContext();
        String uploadInitKey = servletContext.getInitParameter(MULTIPART_CONFIG_PARAM);
        LOGGER.info("uploadInitKey:" + uploadInitKey);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(uploadInitKey), "The %s must be set in web.xml with context-param", MULTIPART_CONFIG_PARAM);

        JSONObject config = JSONObject.parseObject(configService.get(uploadInitKey));
        JSONArray servletArray = config.getJSONArray(CONF_SERVLETS);

        Preconditions.checkArgument(servletArray != null && servletArray.size() > 0, "No %s config founded", CONF_SERVLETS);
        List<ServletConfig> servlets = ListUtil.transform(servletArray, new Config2ServletEntryFunc());

        Injector injector = guiceInjector.getInjector();
        for (ServletConfig servletConfig : servlets) {
            injector.injectMembers(servletConfig.getServlet());
        }

        int i = 10;
        for (ServletConfig servletConfig : servlets) {
            LOGGER.info("Register servlet:{}", servletConfig);
            ServletRegistration.Dynamic reg = servletContext.addServlet(servletConfig.getName(), servletConfig.getServlet());
            reg.setLoadOnStartup(i++);
            reg.addMapping(servletConfig.getUrl());
            reg.setAsyncSupported(servletConfig.isAsync());
            reg.setMultipartConfig(servletConfig.getMultipartConfigElement());
        }

    }

    @Override
    public synchronized void contextDestroyed(ServletContextEvent sce) {
    }
}

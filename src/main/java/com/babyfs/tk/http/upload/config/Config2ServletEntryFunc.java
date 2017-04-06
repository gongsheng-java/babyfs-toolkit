package com.babyfs.tk.http.upload.config;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.servlet.MultipartConfigElement;
import javax.servlet.Servlet;
import java.io.File;
import java.util.Map;

/**
 * 将json配置中的servlet配置转换为{@link ServletConfig}
 */
public class Config2ServletEntryFunc implements Function<Object, ServletConfig> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Config2ServletEntryFunc.class);

    public static final String SERVLET_ASYNC = "async";
    public static final String SERVLET_URL = "url";
    public static final String SERVLET_MULTIPART = "multipart";
    public static final String SERVLET_MULTIPART_LOCATION = "multipart.location";
    public static final String SERVLET_MULTIPART_MAX_FILE_SIZE = "multipart.maxFileSize";
    public static final String SERVLET_MULTIPART_MAX_REQUEST_SIZE = "multipart.maxRequestSize";
    public static final String SERVLET_MULTIPART_FILE_SIZE_THRESHOLD = "multipart.fileSizeThreshold";

    @Override
    public ServletConfig apply(@Nonnull Object input) {
        Preconditions.checkArgument(input instanceof JSONObject, "The input should be a JsonObject");
        JSONObject jsonObject = (JSONObject) input;

        String servletClassName = jsonObject.getString("class");
        String servletName = jsonObject.getString("name");
        Servlet servlet;
        try {
            servlet = (Servlet) Class.forName(servletClassName).newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Can't create the servlet for class name [" + servletClassName + "]");
        }

        ServletConfig entry = new ServletConfig();
        entry.setServlet(servlet);
        entry.setName(servletName);
        entry.setUrl(jsonObject.getString(SERVLET_URL));
        entry.setAsync(getValue(jsonObject, SERVLET_ASYNC, false));
        boolean useMultiPart = getValue(jsonObject, SERVLET_MULTIPART, false);
        if (useMultiPart) {
            String location = getValue(jsonObject, SERVLET_MULTIPART_LOCATION, null);
            File locationFile = new File(location);
            if (!locationFile.exists()) {
                LOGGER.info("Create {} dir.", location);
                if (!locationFile.mkdirs()) {
                    LOGGER.warn("Can't create {} dir.", location);
                    throw new IllegalStateException("Can't crate dir " + location);
                }
            }
            long maxFileSize = ((Number) getValue(jsonObject, SERVLET_MULTIPART_MAX_FILE_SIZE, -1L)).longValue();
            long maxRequestSize = ((Number) getValue(jsonObject, SERVLET_MULTIPART_MAX_REQUEST_SIZE, -1L)).longValue();
            int fileSizeThreshold = ((Number) getValue(jsonObject, SERVLET_MULTIPART_FILE_SIZE_THRESHOLD, 0)).intValue();
            MultipartConfigElement multipartConfigElement = new MultipartConfigElement(location, maxFileSize, maxRequestSize, fileSizeThreshold);
            entry.setMultipartConfigElement(multipartConfigElement);
        } else {
            entry.setMultipartConfigElement(null);
        }
        return entry;
    }

    private static <T> T getValue(Map<String, Object> map, String key, T defaultValue) {
        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        } else {
            return (T) value;
        }
    }
}

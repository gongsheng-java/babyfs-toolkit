package com.babyfs.tk.http.upload.config;

import javax.servlet.MultipartConfigElement;
import javax.servlet.Servlet;

/**
 * Servlet配置
 */
public class ServletConfig {
    private Servlet servlet;
    private String name;
    private String url;
    private boolean async;
    private MultipartConfigElement multipartConfigElement;

    public Servlet getServlet() {
        return servlet;
    }

    public void setServlet(Servlet servlet) {
        this.servlet = servlet;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    public MultipartConfigElement getMultipartConfigElement() {
        return multipartConfigElement;
    }

    public void setMultipartConfigElement(MultipartConfigElement multipartConfigElement) {
        this.multipartConfigElement = multipartConfigElement;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ServletEntry");
        sb.append("{servlet=").append(servlet);
        sb.append(", name='").append(name).append('\'');
        sb.append(", url='").append(url).append('\'');
        sb.append(", async=").append(async);
        sb.append(", multipartConfigElement=");
        if (multipartConfigElement != null) {
            sb.append("{location=").append(multipartConfigElement.getLocation());
            sb.append(",maxFilesize=").append(multipartConfigElement.getMaxFileSize());
            sb.append(",fileSizeThreshold=").append(multipartConfigElement.getFileSizeThreshold());
            sb.append(",maxRequestSize=").append(multipartConfigElement.getMaxRequestSize());
            sb.append('}');
        }
        sb.append('}');
        return sb.toString();
    }
}

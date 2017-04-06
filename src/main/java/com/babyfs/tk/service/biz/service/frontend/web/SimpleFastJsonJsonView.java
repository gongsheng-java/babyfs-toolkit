package com.babyfs.tk.service.biz.service.frontend.web;

import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializeFilter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.babyfs.tk.service.basic.utils.JSONUtil;
import org.springframework.web.servlet.view.AbstractView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

/**
 * 参考{@link com.alibaba.fastjson.support.spring.FastJsonJsonView}实现的View
 */
public class SimpleFastJsonJsonView extends AbstractView {

    public static final String DEFAULT_CONTENT_TYPE = "application/json";

    public static final Charset UTF8 = Charset.forName("UTF-8");

    private Charset charset = UTF8;

    private SerializeConfig config;

    private List<SerializeFilter> filters;

    private SerializerFeature[] serializerFeatures = null;

    private boolean disableCaching = true;

    private boolean updateContentLength = false;

    private Object model;

    public SimpleFastJsonJsonView() {
        setContentType(DEFAULT_CONTENT_TYPE);
        setCharset(UTF8);
    }

    public void setSerializerFeature(SerializerFeature... features) {
        this.setFeatures(features);
    }

    public Charset getCharset() {
        return this.charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public SerializeConfig getConfig() {
        return config;
    }

    public void setConfig(SerializeConfig config) {
        this.config = config;
    }

    public List<SerializeFilter> getFilters() {
        return filters;
    }

    public void setFilters(List<SerializeFilter> filters) {
        this.filters = filters;
    }

    public SerializerFeature[] getFeatures() {
        return serializerFeatures;
    }

    public void setFeatures(SerializerFeature... features) {
        this.serializerFeatures = features;
    }

    public Object getModel() {
        return model;
    }

    public void setModel(Object model) {
        this.model = model;
    }

    @Override
    protected void renderMergedOutputModel(Map<String, Object> m, HttpServletRequest request,
                                           HttpServletResponse response) throws Exception {

        String text = JSONUtil.toJSONString(this.model, config, filters, serializerFeatures);
        byte[] bytes = text.getBytes(charset);

        OutputStream stream = this.updateContentLength ? createTemporaryOutputStream() : response.getOutputStream();
        stream.write(bytes);

        if (this.updateContentLength) {
            writeToResponse(response, (ByteArrayOutputStream) stream);
        }
    }

    @Override
    protected void prepareResponse(HttpServletRequest request, HttpServletResponse response) {
        setResponseContentType(request, response);
        response.setCharacterEncoding(UTF8.name());
        if (this.disableCaching) {
            response.addHeader("Pragma", "no-cache");
            response.addHeader("Cache-Control", "no-cache, no-store, max-age=0");
            response.addDateHeader("Expires", 1L);
        }
    }

    /**
     * Disables caching of the generated JSON.
     * <p/>
     * Default is {@code true}, which will prevent the client from caching the generated JSON.
     */
    public void setDisableCaching(boolean disableCaching) {
        this.disableCaching = disableCaching;
    }

    /**
     * Whether to update the 'Content-Length' header of the response. When set to {@code true}, the response is buffered
     * in order to determine the content length and set the 'Content-Length' header of the response.
     * <p/>
     * The default setting is {@code false}.
     */
    public void setUpdateContentLength(boolean updateContentLength) {
        this.updateContentLength = updateContentLength;
    }
}

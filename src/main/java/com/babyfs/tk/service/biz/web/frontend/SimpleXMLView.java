package com.babyfs.tk.service.biz.web.frontend;

import org.springframework.web.servlet.view.AbstractView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * 简单的XML View
 */
public class SimpleXMLView extends AbstractView {

    public static final String DEFAULT_CONTENT_TYPE = "text/xml";

    public static final Charset UTF8 = Charset.forName("UTF-8");

    private Charset charset = UTF8;

    private boolean disableCaching = true;

    private boolean updateContentLength = false;

    private String xml;

    public SimpleXMLView() {
        setContentType(DEFAULT_CONTENT_TYPE);
        setCharset(UTF8);
    }

    public Charset getCharset() {
        return this.charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    @Override
    protected void renderMergedOutputModel(Map<String, Object> m, HttpServletRequest request, HttpServletResponse response) throws Exception {
        byte[] bytes = xml.getBytes(charset);
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

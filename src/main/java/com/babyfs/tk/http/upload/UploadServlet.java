package com.babyfs.tk.http.upload;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.google.inject.Inject;
import com.babyfs.tk.commons.Constants;
import com.babyfs.tk.commons.base.Pair;
import com.babyfs.tk.http.upload.service.IUploadService;
import com.babyfs.tk.http.upload.util.Util;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 基本的文件上传的入口servlet
 */
public class UploadServlet extends HttpServlet implements AsyncListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(UploadServlet.class);

    @Inject
    private IUploadService uploadService;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String method = req.getMethod();
        if (!"POST".equals(method)) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Only POST");
            return;
        }
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String contentType = req.getContentType();
        if (Strings.isNullOrEmpty(contentType) || !(contentType.startsWith("multipart/form-data"))) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Only multipart support.");
            return;
        }
        Map<String, List<String>> parameters = Maps.newHashMap();
        List<Pair<String, Part>> files = Lists.newArrayList();
        try {
            String queryString = req.getQueryString();
            if (!Strings.isNullOrEmpty(queryString)) {
                //解析Url中的参数
                fillQueryString(queryString, parameters);
            }
            Collection<Part> parts = req.getParts();
            if (parts == null || parts.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Empty request parts.");
                return;
            }

            for (Part part : parts) {
                final String name = part.getName();
                final String contentDisposition = part.getHeader("content-disposition");
                if (Strings.isNullOrEmpty(contentDisposition) || part.getSize() == 0 || Strings.isNullOrEmpty(name)) {
                    continue;
                }
                String partContentType = part.getContentType();
                String fileName = Util.getFileName(contentDisposition);
                if (fileName == null && partContentType == null) {
                    //没有文件名,且contentType也是空的,认为这是一个普通的form field
                    ByteArrayOutputStream bout = new ByteArrayOutputStream();
                    InputStream partInputStream = null;
                    try {
                        partInputStream = part.getInputStream();
                        ByteStreams.copy(partInputStream, bout);
                    } finally {
                        Closeables.close(partInputStream, false);
                    }
                    String value = new String(bout.toByteArray(), "UTF-8");
                    putParameter(parameters, name, value);
                } else if (!Strings.isNullOrEmpty(fileName) && partContentType != null) {
                    //有文件名,并且partContentType不为空才作为文件处理
                    files.add(Pair.of(fileName, part));
                }
            }

            if (files.isEmpty()) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "No files");
                return;
            }

            //校验参数
            Pair<Boolean, Object> validate = uploadService.validate(parameters, files, req);
            if (validate == null || !validate.first) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Parameters is illegal.");
                return;
            }

            //存储上传文件
            Map<String, Object> storeResult = Maps.newHashMap();
            for (Pair<String, Part> file : files) {
                String fileName = file.getFirst();
                Part filePart = file.getSecond();
                Object result = uploadService.store(filePart, fileName, parameters, validate);
                if (result == null) {
                    sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Can't store the file.");
                    return;
                }
                storeResult.put(filePart.getName(), result);
            }
            // 异步处理请求
            AsyncContext asyncContext = req.startAsync();
            asyncContext.addListener(this);
            asyncContext.setTimeout(3000);
            if (!uploadService.process(parameters, validate, storeResult, asyncContext)) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Process fail,can't upload.");
            }
        } catch (Exception e) {
            LOGGER.error("Upload error", e);
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Something is error,can't upload.");
        } finally {
            for (Pair<String, Part> file : files) {
                try {
                    file.getSecond().delete();
                } catch (IOException e) {
                    LOGGER.error("Delete " + file.getSecond() + " fail", e);
                }
            }
        }
    }


    @Override
    public void onComplete(AsyncEvent event) throws IOException {

    }

    @Override
    public void onTimeout(AsyncEvent event) throws IOException {
        LOGGER.warn("Timeout event:" + event);
        ServletResponse suppliedResponse = event.getSuppliedResponse();
        ((HttpServletResponse) suppliedResponse).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Timeout");
    }

    @Override
    public void onError(AsyncEvent event) throws IOException {
        LOGGER.warn("Error event:" + event);
        ServletResponse suppliedResponse = event.getSuppliedResponse();
        ((HttpServletResponse) suppliedResponse).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error");
    }

    @Override
    public void onStartAsync(AsyncEvent event) throws IOException {
    }

    /**
     * 设置参数
     *
     * @param parameterMap
     * @param name
     * @param value
     */
    private void putParameter(Map<String, List<String>> parameterMap, String name, String value) {
        List<String> values = parameterMap.get(name);
        if (values == null) {
            values = Lists.newArrayList();
            parameterMap.put(name, values);
        }
        values.add(value);
    }

    /**
     * 将URL中的查询参数填充到map中
     *
     * @param queryString
     * @param parameters
     */
    private void fillQueryString(String queryString, Map<String, List<String>> parameters) {
        List<NameValuePair> nameValuePairs = URLEncodedUtils.parse(queryString, Constants.DEFAULT_CHARSET_OBJ);
        for (NameValuePair pair : nameValuePairs) {
            List<String> strings = parameters.get(pair.getName());
            if (strings == null) {
                strings = Lists.newArrayList();
                parameters.put(pair.getName(), strings);
            }
            strings.add(pair.getValue());
        }
    }

    /**
     * 发送错误响应
     *
     * @param resp
     * @param code
     * @param msg
     */
    private void sendError(HttpServletResponse resp, int code, String msg) {
        try {
            resp.sendError(code, msg);
        } catch (IOException e) {
            LOGGER.error("Send error fail", e);
        }
    }
}

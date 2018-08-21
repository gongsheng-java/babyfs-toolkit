package com.babyfs.tk.service.biz.web.backend;

import com.alibaba.fastjson.JSON;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.UUID;

/**
 * Class LogFilter
 * Author zhanghongyun
 * Date 2018-08-21
 */
@Component
@WebFilter(filterName = "logFilter",urlPatterns = "/*")
public class LogFilter implements Filter {

    static final Log logger = LogFactory.getLog(LogFilter.class);
    static final String ignoreUrlRegex = ".*((pay/)|(/index)|(/index/.*)|([.]((html)|(jsp)|(css)|(js)|(gif)|(png))))$";
    static final String exportContentType = "application/vnd.ms-excel";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String traceId = UUID.randomUUID().toString();
        ResponseWrapper responseWrapper = new ResponseWrapper((HttpServletResponse) response);
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        RequestWrapper requestWrapper = new RequestWrapper(httpServletRequest);

        String url = httpServletRequest.getRequestURI();
        try {
            // 请求html页面、js不打印日志
            if (url.matches(ignoreUrlRegex)) {
                chain.doFilter(requestWrapper, responseWrapper);
                return;
            }

            // 打印form格式的入参信息
            Map params = request.getParameterMap();
            if (null != params && params.size() != 0) {
                logger.info(String.format("%s parameters：%s", traceId, JSON.toJSONString(params)));
            } else {
                // 打印json格式的入参信息
                String charEncoding = requestWrapper.getCharacterEncoding() != null ? requestWrapper.getCharacterEncoding() : "UTF-8";
                logger.info(String.format("%s parameters：%s", traceId, new String(requestWrapper.toByteArray(), charEncoding)));
            }

            chain.doFilter(requestWrapper, responseWrapper);

            String outParam = null;
            if (responseWrapper.getContentType() != exportContentType) {
                outParam = new String(responseWrapper.getBytes(), responseWrapper.getCharacterEncoding());
                logger.info(String.format("%s result：%s", traceId, outParam));

                writeResponse(response,outParam);
            }


        }
        catch (Exception ex) {
            logger.error(String.format("%s logfilter error",traceId),ex);
        }
    }

    @Override
    public void destroy() {

    }

    private void writeResponse(ServletResponse response, String responseString)
            throws IOException {
        PrintWriter out = response.getWriter();
        out.print(responseString);
        out.flush();
        out.close();
    }
}

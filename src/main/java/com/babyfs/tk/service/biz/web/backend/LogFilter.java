package com.babyfs.tk.service.biz.web.backend;

import com.alibaba.fastjson.JSON;
import com.babyfs.tk.commons.model.ServiceResponse;
import com.babyfs.tk.service.biz.base.model.ParsedEntity;
import com.babyfs.tk.service.biz.kvconf.IKVConfService;
import com.babyfs.tk.service.biz.kvconf.model.KVConfEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.inject.Inject;
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
    static final String swithName = "_sys.toolkit.logfilter.switch";

    Boolean logSwitch = null;
    @Inject
    IKVConfService kvConfService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    private void getSwitch(ServletRequest request) {
        try {
            if(kvConfService==null) {
                ApplicationContext ac1 = WebApplicationContextUtils.getRequiredWebApplicationContext(request.getServletContext());
                kvConfService = ac1.getBean(IKVConfService.class);
            }
            ServiceResponse<ParsedEntity<KVConfEntity, Object>> resp = kvConfService.getByNameWithLocalCache(swithName);

            if (resp.isFailure()) {
                logSwitch = false;
            }
            logSwitch = (Boolean) resp.getData().getParsed();
        }
        catch (Exception ex){
            logSwitch = false;
            logger.warn(String.format("获取配置kv-%s，错误",swithName),ex);
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        getSwitch(request);
        String traceId = UUID.randomUUID().toString();
        ResponseWrapper responseWrapper = new ResponseWrapper((HttpServletResponse) response);
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        RequestWrapper requestWrapper = new RequestWrapper(httpServletRequest);

        //开关关闭，不打印请求日志
        if(!logSwitch) {
            chain.doFilter(request, response);
            return;
        }

        String url = httpServletRequest.getRequestURI();
        try {
            // 请求html页面、js不打印日志
            if (url.matches(ignoreUrlRegex)) {
                chain.doFilter(request, response);
                return;
            }

            // 打印form格式的入参信息
            Map params = request.getParameterMap();
            if (null != params && params.size() != 0) {
                logger.info(String.format("%s url:%s;parameters：%s", traceId, url, JSON.toJSONString(params)));
            } else {
                // 打印json格式的入参信息
                String charEncoding = requestWrapper.getCharacterEncoding() != null ? requestWrapper.getCharacterEncoding() : "UTF-8";
                logger.info(String.format("%s parameters：%s", traceId, new String(requestWrapper.toByteArray(), charEncoding)));
            }

            chain.doFilter(requestWrapper, responseWrapper);

            String outParam = null;
            outParam = new String(responseWrapper.getBytes(), responseWrapper.getCharacterEncoding());
            writeResponse(response, outParam);

            if (outParam != null && outParam != "" && responseWrapper.getContentType() != exportContentType) {
                if (outParam.length() > 200)
                    outParam = outParam.substring(0, 200) + "...";
                logger.info(String.format("%s result：%s", traceId, outParam));
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

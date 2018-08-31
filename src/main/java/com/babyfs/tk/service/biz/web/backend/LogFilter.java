package com.babyfs.tk.service.biz.web.backend;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.babyfs.tk.commons.model.ServiceResponse;
import com.babyfs.tk.service.biz.base.model.ParsedEntity;
import com.babyfs.tk.service.biz.kvconf.IKVConfService;
import com.babyfs.tk.service.biz.kvconf.model.KVConfEntity;
import com.google.common.base.Strings;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
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
    static String ignoreUrlRegex = ".*((pay/)|(/index)|(/rpc)|(/index/.*)|([.]((html)|(jsp)|(css)|(js)|(gif)|(png))))$";
    static final String exportContentType = "application/vnd.ms-excel";
    static final String swithName = "_sys.toolkit.logfilter.switch";
    static final int defaultLength = 500;

    Boolean logSwitch = null;
    int maxLength;
    @Inject
    IKVConfService kvConfService;

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    //获取替换配置
    private void getConfig(ServletRequest request) {

        try {
            if(kvConfService==null) {
                ApplicationContext ac1 = WebApplicationContextUtils.getRequiredWebApplicationContext(request.getServletContext());
                kvConfService = ac1.getBean(IKVConfService.class);
            }
            ServiceResponse<ParsedEntity<KVConfEntity, Object>> resp = kvConfService.getByNameWithLocalCache(swithName);

            if (resp.isFailure()) {
                logSwitch = false;
            }
            JSONObject object = (JSONObject) resp.getData().getParsed();
            logSwitch = object.getBoolean("logSwitch");
            maxLength = object.getIntValue("maxLength");
            if(!Strings.isNullOrEmpty(object.getString("ignoreUrl"))){
                ignoreUrlRegex = object.getString("ignoreUrl");
            }
        }
        catch (Exception ex){
            logSwitch = false;
            logger.warn(String.format("获取配置kv-%s，错误",swithName),ex);
        }

        if(logSwitch==null)
            logSwitch = false;
        if(maxLength==0)
            maxLength = defaultLength;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        getConfig(request);
        String traceId = UUID.randomUUID().toString();
        ResponseWrapper responseWrapper = new ResponseWrapper((HttpServletResponse) response);
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(httpServletRequest);

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

            long startStamps = System.currentTimeMillis();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(traceId);

            // 打印form格式的入参信息
            Map params = request.getParameterMap();
            if (null != params && params.size() != 0) {
                stringBuilder.append(String.format(" url:%s;getParameterMaps:%s", url, JSON.toJSONString(params)));
            } else {
                // 打印json格式的入参信息
                String charEncoding = requestWrapper.getCharacterEncoding() != null ? requestWrapper.getCharacterEncoding() : "UTF-8";
                stringBuilder.append(String.format(" url:%s;requestWrapper:%s", url, new String(requestWrapper.getContentAsByteArray(), charEncoding)));
            }

            //打印header信息
            HashMap<String, String> headerMap = (HashMap<String, String>) getHeaderMap(httpServletRequest);
            if (headerMap != null)
                stringBuilder.append(String.format(" header:%s", JSON.toJSONString(headerMap)));

            chain.doFilter(requestWrapper, responseWrapper);

            byte[] respNew = responseWrapper.getBytes();

            String outParam = new String(respNew, responseWrapper.getCharacterEncoding());

            if (outParam != null && outParam != "" && responseWrapper.getContentType() != exportContentType) {
                if (outParam.length() > maxLength)
                    outParam = outParam.substring(0, maxLength) + "...";
                stringBuilder.append(String.format(" result:%s", outParam));
            }

            long duration = System.currentTimeMillis() - startStamps;
            stringBuilder.append(String.format(" time:%s",duration));

            if (stringBuilder.length() > 0)
                logger.info(stringBuilder.toString());
            if (!response.isCommitted()) {

                //会写response
                response.setCharacterEncoding(responseWrapper.getCharacterEncoding());
                writeResponse(response, respNew);
            }

        }
        catch (Exception ex) {
            logger.error(String.format("%s logfilter error",traceId),ex);
        }
    }

    @Override
    public void destroy() {

    }

    private void writeResponse(ServletResponse response, byte[] respnew)
            throws IOException {
        ServletOutputStream out = response.getOutputStream();
        out.write(respnew);
        out.flush();
    }

    private Map<String,String> getHeaderMap(HttpServletRequest request){
        Map<String, String> map = new HashMap<String, String>();
        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {//循环遍历Header中的参数，把遍历出来的参数放入Map中
            String key = (String) headerNames.nextElement();
            String value = ((HttpServletRequest) request).getHeader(key);
            map.put(key, value);
        }
        return map;
    }
}

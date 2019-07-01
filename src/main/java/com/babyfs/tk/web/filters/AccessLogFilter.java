package com.babyfs.tk.web.filters;

import com.babyfs.servicetk.apollo.ConfigLoader;
import com.babyfs.tk.safes.Safes;
import com.babyfs.tk.trace.TraceConstant;
import com.babyfs.tk.trace.TraceGenerator;
import com.babyfs.tk.web.cache.CachedHttpServletRequestWrapper;
import com.babyfs.tk.web.cache.CachedHttpServletResponseWrapper;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author gaowei
 * @date 2018/12/20
 */
public class AccessLogFilter extends OncePerRequestFilter {

    private static Logger logger = LoggerFactory.getLogger(AccessLogFilter.class);

    private static final int MAX_CACHE_LEN = 2 * 1024 * 1024;

    private static final int INIT_CACHE_LEN = 512 * 1024;

    //apollo config key
    private static final String EXCLUDE_CONFIG_KEY = "api.log.exclude";

    //exclude set, refresh 5 mins
    private static Supplier<Set<String>> API_EXCLUDE_CACHE = Suppliers
            .memoizeWithExpiration(excludeUrlSupplier(), 5, TimeUnit.MINUTES);


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();

        //放置trace到MDC
        putTrace(request);

        CachedHttpServletRequestWrapper requestWrapper = new CachedHttpServletRequestWrapper(request, INIT_CACHE_LEN, MAX_CACHE_LEN);
        CachedHttpServletResponseWrapper responseWrapper = new CachedHttpServletResponseWrapper(response, INIT_CACHE_LEN, MAX_CACHE_LEN);
        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long endTime = System.currentTimeMillis();
            // elapsed time
            long elapsed = endTime - startTime;
            //log
            logInternal(requestWrapper, responseWrapper, elapsed);
            //clear
            MDC.clear();
        }
    }

    /**
     * 从HttpHeader拿到trace
     *
     * @param request
     */
    private void putTrace(HttpServletRequest request) {
        String traceIdHeader = request.getHeader(TraceConstant.NAME);
        if (StringUtils.isBlank(traceIdHeader)) {
            traceIdHeader = TraceGenerator.generateTrace();
        }
        MDC.put(TraceConstant.NAME, traceIdHeader);
    }

    /**
     * 请求日志
     *
     * @param request
     * @param response
     * @param elapsed  耗时
     */
    protected void logInternal(CachedHttpServletRequestWrapper request, CachedHttpServletResponseWrapper response, long elapsed) {
        String requestURI = request.getRequestURI();
        //查询参数
        String queryString = buildQueryString(request);

        String requestPayload = getMessagePayload(request);
        String responsePayload = getMessagePayload(response);

        //exclude
        Set<String> excludeSet = API_EXCLUDE_CACHE.get();
        if (excludeSet.contains(requestURI)) {
            logger.info("uri={};query={};elapsed={}ms;skipped", requestURI, queryString, elapsed);
            return;
        }

        logger.info("uri={};query={};elapsed={}ms;request={};response={}", requestURI, queryString, elapsed, requestPayload, responsePayload);
    }

    /**
     * 构造查询参数
     *
     * @param request
     * @return
     */
    private String buildQueryString(CachedHttpServletRequestWrapper request) {
        StringBuilder queryStringBuilder = new StringBuilder();
        int startIndex = 0;
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String paramValue = request.getParameter(paramName);
            queryStringBuilder.append("&" + paramName + "=" + paramValue);
            startIndex = 1;
        }
        return queryStringBuilder.substring(startIndex);
    }

    protected String getMessagePayload(CachedHttpServletRequestWrapper request) {
        String payload = "";
        if (request != null) {
            byte[] buf = request.getCachedStream().getCached();
            payload = byteBufferToString(buf, request.getCharacterEncoding());
        }
        return payload;
    }

    protected String getMessagePayload(CachedHttpServletResponseWrapper response) {
        String payload = "";
        if (response != null) {
            byte[] buf = response.getCachedStream().getCached();
            payload = byteBufferToString(buf, response.getCharacterEncoding());
        }
        return payload;
    }

    private String byteBufferToString(byte[] buf, String encoding) {
        if (Objects.nonNull(buf) && buf.length > 0) {
            try {
                if(StringUtils.isBlank(encoding)) {
                    return new String(buf, 0, buf.length);
                }else {
                    return new String(buf, 0, buf.length, encoding);
                }
            } catch (UnsupportedEncodingException ex) {
                return "[unknown]";
            }
        }
        return "";
    }

    private static Supplier<Set<String>> excludeUrlSupplier() {
        return () -> {
            Set<String> excludeSet = new HashSet<>();
            try {
                String exclude = ConfigLoader.getConfig(EXCLUDE_CONFIG_KEY);
                Collections.addAll(excludeSet, Safes.of(exclude).split(","));
            } catch (Exception e) {
                logger.warn("加载api.log.exclude到缓存失败:", e);
            }
            return excludeSet;
        };
    }
}
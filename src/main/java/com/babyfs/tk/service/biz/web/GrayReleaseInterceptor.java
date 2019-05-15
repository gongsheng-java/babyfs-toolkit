package com.babyfs.tk.service.biz.web;

import com.babyfs.servicetk.grpcapicore.gray.GrayContext;
import org.elasticsearch.common.Strings;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用于Spring的权限过滤器
 */
@Component
@Order(1)
public class GrayReleaseInterceptor extends HandlerInterceptorAdapter {
    private static final String KEY_GRAY_FLAG = "gray";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String header = request.getHeader(KEY_GRAY_FLAG);
        if(!Strings.isNullOrEmpty(header)){
            GrayContext.set(header);
        }
        return super.preHandle(request, response, handler);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        GrayContext.remove();
        super.postHandle(request, response, handler, modelAndView);
    }
}

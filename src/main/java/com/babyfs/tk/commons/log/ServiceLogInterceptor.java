package com.babyfs.tk.commons.log;

import com.alibaba.fastjson.JSON;
import com.babyfs.tk.commons.log.annotation.ServiceLog;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;


/*
*
* Service方法拦截打印日志
*
* */

public class ServiceLogInterceptor implements MethodInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(ServiceLogInterceptor.class);

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        Object returnValue = null;

        boolean logRequest = true;
        boolean logResponse = true;

        ServiceLog logConfig = methodInvocation.getMethod().getAnnotation(ServiceLog.class);
        if(logConfig != null) {
            logRequest = logConfig.requestLog();
            logResponse = logConfig.responseLog();
        }

        StringBuffer sbLog = new StringBuffer();

        //记录方法名
        sbLog.append(String.format("ServiceLog:%s.%s",
                methodInvocation.getMethod().getDeclaringClass().getSimpleName(),
                methodInvocation.getMethod().getName()));

        //记录请求参数
        if(logRequest) {
            Object[] arguments = methodInvocation.getArguments();
            if (arguments != null) {
                for (int i = 0; i < arguments.length; i++) {
                    sbLog.append(String.format(", arg%d:%s", i, JSON.toJSON(arguments[i])));
                }
            }
        }

        Long begin = System.currentTimeMillis();
        returnValue = methodInvocation.proceed();
        Long timespan = System.currentTimeMillis() - begin;

        //记录返回参数
        if(logResponse && isLogByRate(logConfig.rate())) {
            sbLog.append(String.format(", response:%s", JSON.toJSON(returnValue)));
        }

        //记录响应时长
        sbLog.append(String.format(", cost:%s", timespan));

        logger.info(sbLog.toString());
        return returnValue;
    }

    /*
    * 按照打印频率计算是否需要打印返回结果
    * */
    private boolean isLogByRate(int rate) {
        if(rate <= 0) {
            return true;
        }

        if(rate >= 10) {
            return false;
        }

        Random r = new Random();
        return rate <= r.nextInt(10);
    }
}

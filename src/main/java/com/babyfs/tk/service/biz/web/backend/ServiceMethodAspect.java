package com.babyfs.tk.service.biz.web.backend;

import com.alibaba.fastjson.JSONObject;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.aspectj.lang.reflect.CodeSignature;

import java.util.List;

/**
 * @ClassName ServiceMethodAspect
 * @Author zhanghongyun
 * @Date 2018/9/10 上午10:26
 **/
@Aspect
@Component
@Order(9999)
public class ServiceMethodAspect {
    private static final Logger _logger = LoggerFactory.getLogger(ServiceMethodAspect.class);

    @Around(value = "execution(public * com.babyfs..*.*Impl.*(..))")
    public Object MethodAccess(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        JoinPoint.StaticPart thisJoinPointStaticPart = proceedingJoinPoint.getStaticPart();
        Object[] paramValues = proceedingJoinPoint.getArgs();
        String[] paramNames = ((CodeSignature) thisJoinPointStaticPart
                .getSignature()).getParameterNames();
        StringBuilder logLine = new StringBuilder(thisJoinPointStaticPart
                .getSignature().getName()).append("(");
        if (paramNames.length != 0)
            logParamValues(logLine, paramNames, paramValues);
        logLine.append(") - finished");
        return proceedingJoinPoint.proceed();
    }

    private static void logParamValues(StringBuilder logLine,
                                      String[] paramNames, Object[] paramValues) {
        for (int i = 0; i < paramValues.length; i++) {
            logLine.append(paramNames[i]).append("=")
                    .append(toString(paramValues[i]));
            if (i < paramValues.length - 1)
                logLine.append(", ");
        }
    }


    @SuppressWarnings("rawtypes")
    public static String toString(Object object) {
        if (object == null)
            return "<null>";
        else if (object instanceof String) {
            if(((String) object).length() > 100)
                return ((String) object).substring(0, 100) + "...[more]";
            else return (String) object;
        }
        else if (object instanceof Long)
            return ((Long) object).toString();
        else if (object instanceof Boolean)
            return ((Boolean) object).toString();
        else if (object instanceof Double)
            return ((Double) object).toString();
        else if (object instanceof Integer)
            return ((Integer) object).toString();
        else
            return JSONObject.toJSONString(object);
    }


}

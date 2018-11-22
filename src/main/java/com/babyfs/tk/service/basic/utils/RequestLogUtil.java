package com.babyfs.tk.service.basic.utils;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

import static com.babyfs.tk.service.basic.utils.ResponseUtil.REP_KEY_CODE;
import static com.babyfs.tk.service.basic.utils.ResponseUtil.REP_KEY_MSG;
import static com.babyfs.tk.service.basic.utils.ResponseUtil.REP_KEY_SUCCESS;
import static net.logstash.logback.marker.Markers.append;

/**
 * 写requestLog
 */
public class RequestLogUtil {

    private static String appName = null;

    static {
        init();
    }

    private static void init(){
        try{
            String jettyBase = System.getProperty("jetty.base");
            String[] splits = jettyBase.split(File.separator);
            appName = splits[splits.length - 2];
        }catch (Exception e){
            appName = "unknown";//any exception give a default name
        }
    }

    private final static Logger logger = LoggerFactory.getLogger(RequestLogUtil.class);

    private static ThreadLocal<LogNode> context = new ThreadLocal<>();

    private static LogNode getLogNode(){
        LogNode logNode = context.get();
        if(logNode == null){
            logNode = new LogNode();
            context.set(logNode);
        }
        return logNode;
    }

    public static void setHttpMethod(String method){
        getLogNode().httpMethod = method;
    }

    public static void setPath(String path){
        getLogNode().path = path;
    }

    public static void setClientIp(String clientIp){
        getLogNode().clientIp = clientIp;
    }

    public static void setResult(Map<String, Object> result){
        if(result == null){
            return;
        }
        LogNode logNode = getLogNode();
        logNode.isActive = true;
        Object isSucessObj = result.get(REP_KEY_SUCCESS);
        if(isSucessObj != null){
            logNode.isSuccess = ((Boolean)isSucessObj).booleanValue();
        }

        Object codeObj = result.get(REP_KEY_CODE);
        if(codeObj != null){
            logNode.statusCode = ((Integer)codeObj).intValue();
        }

        Object msg = result.get(REP_KEY_MSG);
        if(msg != null){
            logNode.msg = (String)msg;
        }
    }
    
    public static void startCount(){
        LogNode logNode = getLogNode();

        logNode.startMilliSecond = System.currentTimeMillis();
        logNode.endMilliSecond = 0;//reset it
    }

    
    public static void endCount(){
        LogNode logNode = getLogNode();
        logNode.endMilliSecond = logNode.endMilliSecond > 0 ? logNode.endMilliSecond : System.currentTimeMillis();
    }

    public static void setException(Exception e){
        LogNode logNode = getLogNode();
        logNode.exceptionMsg = e.getMessage();
        logNode.hasException = true;
        logNode.stacktrace = ExceptionUtils.getStackTrace(e);
    }

    public static void remove(){
        context.remove();
    }

    public static void log(){
        LogNode logNode = getLogNode();

        endCount();
        if(logNode.isActive){
            logger.info(append("logger_type", logNode.logType).
                    and(append("logger_status", logNode.isSuccess ? "1" : '0')).
                    and(append("status_code", logNode.statusCode)).
                    and(append("cost", logNode.endMilliSecond - logNode.startMilliSecond)).
                    and(append("method", logNode.httpMethod)).
                    and(append("path", logNode.path)).
                    and(append("msg", logNode.msg)).
                    and(append("client_ip", logNode.clientIp)).
                    and(append("app_name", appName)).
                    and(append("has_exception", logNode.hasException ? "1" : "0")).
                    and(append("exception_msg", logNode.exceptionMsg)).
                    and(append("stacktrace", logNode.stacktrace)), "");
        }

        context.remove();
    }

    private static class LogNode{
        private String httpMethod = "";

        private String logType = "1";

        private int statusCode = 0;

        private long startMilliSecond = 0;

        private String path = "";

        private long endMilliSecond = 0;

        private boolean isSuccess = false;

        private String msg = "";

        private String clientIp;

        private boolean hasException = false;

        private String exceptionMsg = "";

        private String stacktrace = "";

        private boolean isActive = false;

    }
}

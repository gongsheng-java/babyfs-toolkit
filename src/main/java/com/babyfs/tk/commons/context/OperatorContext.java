package com.babyfs.tk.commons.context;

import com.babyfs.tk.commons.model.Operator;

public class OperatorContext {
    private static final InheritableThreadLocal<Operator> operatorContainer = new InheritableThreadLocal<>();

    /**
     * 当前业务逻辑线程set完毕必须清楚，否则会内存泄漏
     * @param name
     */
    public static void setContext(String name){
        setContext("unkown", name, "source", "token");
    }

    /**
     * 当前业务逻辑线程set完毕必须清楚，否则会内存泄漏
     * @param name
     */
    public static void setContext(String ip, String name, String source, String token){
        Operator operator = new Operator();
        operator.setIp(ip);
        operator.setName(name);
        operator.setSource(source);
        operator.setToken(token);
        operatorContainer.set(operator);
    }

    public static Operator getContext(){
        Operator operator = operatorContainer.get();
        if(operator == null){
            operator = new Operator();
            operator.setToken("unknown");
            operator.setSource("unknown");
            operator.setName("unknown");
            operator.setIp("unknown");
        }
        return operator;
    }

    public static void remove(){
        operatorContainer.remove();
    }
}

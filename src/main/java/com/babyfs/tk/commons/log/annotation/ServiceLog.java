package com.babyfs.tk.commons.log.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/*
*
* 服务日志记录注解
* 通过MethodInterceptor拦截带有@ServiceLog注解的类及方法，并打印入参和出参已经响应时长日志
*
* */

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RUNTIME)
public @interface ServiceLog {
    /*
    * 打印请求参数日志，默认 true
    * */
    boolean requestLog() default true;

    /*
    * 打印返回结果日志，默认 true
    * */
    boolean responseLog() default true;

    /*
    *
    * 返回结果打印频率，对于大对象可以设置低频打印
    * 范围：0～10，数字越小打印频率越高，0表示每次打印，10表示不打印
    * */
    int rate() default 0;

}

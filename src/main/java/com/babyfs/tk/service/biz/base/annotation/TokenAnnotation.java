package com.babyfs.tk.service.biz.base.annotation;

import java.lang.annotation.*;

/**
 * Token注解
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface TokenAnnotation {
    /**
     * 是否需要有效的token,默认为需要
     *
     * @return
     */
    boolean requireAuth() default true;

    /**
     * 是否需要用户的状态有效,默认为不需要
     *
     * @return
     */
    boolean requireValid() default false;

    /**
     * 是否需要检查在线设备,默认检查
     *
     * @return
     */
    boolean checkOnlineDevice() default true;
}

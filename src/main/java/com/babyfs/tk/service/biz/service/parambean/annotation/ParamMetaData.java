package com.babyfs.tk.service.biz.service.parambean.annotation;

import java.lang.annotation.*;

/**
 * 用于配置请求参数验证信息的元数据
 * <p/>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface ParamMetaData {
    String paramName();
    String rule();
    String defaultValue() default "";
}

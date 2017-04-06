package com.babyfs.tk.service.basic.guice.annotation;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 基础INameResourceService服务 ： Redis类型
 * <p/>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.PARAMETER})
@BindingAnnotation
public @interface ServiceRedis {
}

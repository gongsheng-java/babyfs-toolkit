package com.babyfs.tk.commons.service.annotation;

import com.google.inject.BindingAnnotation;
import com.babyfs.tk.commons.service.ILifeService;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * {@link ILifeService}服务的注册注解
 */
@Target({FIELD, PARAMETER, METHOD})
@Retention(RUNTIME)
@BindingAnnotation
public @interface LifecycleServiceRegistry {
}

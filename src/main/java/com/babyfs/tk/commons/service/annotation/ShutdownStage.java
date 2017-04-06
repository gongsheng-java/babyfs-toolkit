package com.babyfs.tk.commons.service.annotation;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 服务的的停止阶段,在此阶段进行服务的清理
 */
@Target({FIELD, PARAMETER, METHOD})
@Retention(RUNTIME)
@BindingAnnotation
public @interface ShutdownStage {
}

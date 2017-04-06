package com.babyfs.tk.commons.service.annotation;

import com.google.inject.BindingAnnotation;
import com.babyfs.tk.commons.service.IStageActionRegistry;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 服务的启动阶段,在此阶段进行服务的初始化工作
 *
 * @see {@link IStageActionRegistry}
 */
@Target({FIELD, PARAMETER, METHOD})
@Retention(RUNTIME)
@BindingAnnotation
public @interface InitStage {
}

package com.babyfs.tk.commons.service.annotation;

import com.google.inject.BindingAnnotation;
import com.babyfs.tk.commons.service.IStageActionRegistry;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 服务的启动完成阶段,在此阶段进行服务的启动完成后的再次进行初始化的工作
 *
 * @see {@link IStageActionRegistry}
 */
@Target({FIELD, PARAMETER, METHOD})
@Retention(RUNTIME)
@BindingAnnotation
public @interface AfterStartStage {
}

package com.babyfs.tk.galaxy;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于标示RPC服务
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@BindingAnnotation
public @interface RpcService {
}

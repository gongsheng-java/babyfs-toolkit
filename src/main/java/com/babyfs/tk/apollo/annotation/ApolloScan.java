package com.babyfs.tk.apollo.annotation;

import com.babyfs.tk.apollo.EnvConstants;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApolloScan {
    String value() default EnvConstants.DEFAULT_NAMESPACE;
}

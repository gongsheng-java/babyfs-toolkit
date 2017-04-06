package com.babyfs.tk.orm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Sql的参数
 */
@Target(value = {ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Shard {
    /**
     * shard的参数的名称
     *
     * @return
     */
    String name();
}

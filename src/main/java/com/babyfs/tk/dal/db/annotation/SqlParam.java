package com.babyfs.tk.dal.db.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Sql的参数,与{@link EntityParam}不能同时使用
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface SqlParam {
    /**
     * 参数的名称
     *
     * @return
     */
    String value();
}

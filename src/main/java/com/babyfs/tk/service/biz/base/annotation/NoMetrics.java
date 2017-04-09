package com.babyfs.tk.service.biz.base.annotation;

import java.lang.annotation.*;

/**
 * 禁止监控
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface NoMetrics {
}

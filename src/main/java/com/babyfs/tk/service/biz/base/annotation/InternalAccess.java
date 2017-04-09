package com.babyfs.tk.service.biz.base.annotation;

import java.lang.annotation.*;

/**
 * 仅限内部IP访问
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface InternalAccess {
}

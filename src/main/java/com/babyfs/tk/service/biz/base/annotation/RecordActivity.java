package com.babyfs.tk.service.biz.base.annotation;

import java.lang.annotation.*;

/**
 * 记录活动日志
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface RecordActivity {
}

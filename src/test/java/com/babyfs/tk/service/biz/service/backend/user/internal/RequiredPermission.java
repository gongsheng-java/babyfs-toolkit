package com.babyfs.tk.service.biz.service.backend.user.internal;

import java.lang.annotation.*;

/**
 * 申明需要的权限
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiredPermission {
    /**
     * 是否需要登录,默认需要登录
     *
     * @return
     */
    boolean loginRequired() default true;

    /**
     * 需要的业务模块的权限
     *
     * @return
     */
    Permissions[] permissions() default {};
}

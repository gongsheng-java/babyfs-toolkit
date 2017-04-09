package com.babyfs.tk.service.biz.op.user.impl;


import com.babyfs.tk.service.biz.op.user.model.OperationType;

import java.lang.annotation.*;

/**
 * 申明需要的业务权限
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Permissions {
    /**
     * 要求的资源
     *
     * @return
     */
    BizResource resource();

    /**
     * 要求的操作
     *
     * @return
     */
    OperationType[] operations() default {};
}

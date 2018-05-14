package com.babyfs.tk.service.biz.op.user.model;

/**
 * 权限
 */
public interface Permission {
    /**
     * 权限的ID
     *
     * @return
     */
    long getId();

    /**
     * 权限对应的资源
     *
     * @return
     */
    Resource getTarget();

    /**
     * 权限的操作
     *
     * @return
     */
    IOperation getOperation();
}

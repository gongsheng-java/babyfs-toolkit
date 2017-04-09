package com.babyfs.tk.service.biz.op.user.model;

/**
 * 业务资源
 */
public interface IBizResource extends Resource {
    /**
     * 父资源
     *
     * @return
     */
    IBizResource getParent();
}

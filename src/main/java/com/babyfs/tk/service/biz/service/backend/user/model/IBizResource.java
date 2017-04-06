package com.babyfs.tk.service.biz.service.backend.user.model;

import com.babyfs.tk.service.biz.service.backend.user.model.bean.Resource;

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

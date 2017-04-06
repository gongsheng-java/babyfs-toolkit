package com.babyfs.tk.service.biz.service.backend.user.model.bean;

import java.util.Set;

/**
 * 主体
 */
public interface Principal {
    /**
     * 主体的名称
     * @return
     */
    String getName();

    /**
     *
     * @return
     */
    Set<Role> getRoles();
}

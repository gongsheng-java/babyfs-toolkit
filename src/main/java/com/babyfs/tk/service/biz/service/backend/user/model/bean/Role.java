package com.babyfs.tk.service.biz.service.backend.user.model.bean;

import java.util.Set;

/**
 * 角色
 */
public interface Role {
    /**
     * 角色的名称
     *
     * @return
     */
    String getName();

    /**
     * 角色的权限
     *
     * @return
     */
    Set<Permission> getPermissions();
}

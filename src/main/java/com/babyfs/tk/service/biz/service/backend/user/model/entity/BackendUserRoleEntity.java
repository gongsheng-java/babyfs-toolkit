package com.babyfs.tk.service.biz.service.backend.user.model.entity;

import com.babyfs.tk.orm.AutoIdEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;

/**
 * 后台用户角色表 Entity
 */
@Entity
@Table(name = "t_backend_user_role")
public class BackendUserRoleEntity extends AutoIdEntity {

    private static final long serialVersionUID = -3030942069605258021L;

    private long roleId;

    private long backendUserId;

    private Timestamp createTime;

    @Column(name = "role_id")
    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    @Column(name = "backend_user_id")
    public long getBackendUserId() {
        return backendUserId;
    }

    public void setBackendUserId(long backendUserId) {
        this.backendUserId = backendUserId;
    }

    @Column(name = "create_time")
    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }
}

package com.babyfs.tk.service.biz.service.backend.user.model.entity;

import com.babyfs.tk.orm.AutoIdEntity;
import com.babyfs.tk.service.biz.service.backend.user.model.bean.Operation;
import com.babyfs.tk.service.biz.service.backend.user.model.bean.Permission;
import com.babyfs.tk.service.biz.service.backend.user.model.bean.Resource;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;

/**
 * 角色权限表Entity
 */
@Entity
@Table(name = "t_role_permission")
public class RolePermissionEntity extends AutoIdEntity implements Permission {
    private static final long serialVersionUID = 7745983202775286248L;

    private long roleId;

    /**
     * 权限资源的类型
     */
    private int permissionResType;

    /**
     * 权限资源的ID
     */
    private String permissionResId;

    /**
     * 权限资源的操作掩码
     *
     */
    private int permissionResOpMask;

    /**
     * 权限资源的扩展属性
     */
    private String permissionResProp;

    private Timestamp createTime;

    private transient Resource target;

    private transient Operation operation;

    @Column(name = "role_id")
    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    @Column(name = "perm_res_type")
    public int getPermissionResType() {
        return permissionResType;
    }

    public void setPermissionResType(int permissionResType) {
        this.permissionResType = permissionResType;
    }

    @Column(name = "perm_res_id")
    public String getPermissionResId() {
        return permissionResId;
    }

    public void setPermissionResId(String permissionResId) {
        this.permissionResId = permissionResId;
    }

    @Column(name = "perm_res_op_mask")
    public int getPermissionResOpMask() {
        return permissionResOpMask;
    }

    public void setPermissionResOpMask(int permissionResOpMask) {
        this.permissionResOpMask = permissionResOpMask;
    }

    @Column(name = "perm_res_prop")
    public String getPermissionResProp() {
        return permissionResProp;
    }

    public void setPermissionResProp(String permissionResProp) {
        this.permissionResProp = permissionResProp;
    }

    @Column(name = "create_time")
    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    @Override
    public Resource getTarget() {
        return target;
    }

    public void setTarget(Resource target) {
        this.target = target;
    }

    @Override
    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }
}

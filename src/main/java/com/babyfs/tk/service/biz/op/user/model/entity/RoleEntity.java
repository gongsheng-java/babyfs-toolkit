package com.babyfs.tk.service.biz.op.user.model.entity;

import com.babyfs.tk.dal.orm.AutoIdEntity;
import com.babyfs.tk.service.biz.op.user.model.Permission;
import com.babyfs.tk.service.biz.op.user.model.Role;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.Set;

/**
 * 角色表Entity
 */
@Entity
@Table(name = "t_role")
public class RoleEntity extends AutoIdEntity implements Role {

    private static final long serialVersionUID = 5926414603649499310L;

    private String name;

    private String desc;

    private Timestamp createTime;

    private transient Set<Permission> permissions;

    @Column(name = "name")
    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "description")
    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Column(name = "create_time")
    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    @Override
    public Set<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<Permission> permissions) {
        this.permissions = permissions;
    }

}

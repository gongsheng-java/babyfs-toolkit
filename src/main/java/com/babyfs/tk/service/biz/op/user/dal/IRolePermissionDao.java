package com.babyfs.tk.service.biz.op.user.dal;

import com.babyfs.tk.dal.db.IDao;
import com.babyfs.tk.dal.db.annotation.Dao;
import com.babyfs.tk.dal.db.annotation.Sql;
import com.babyfs.tk.dal.db.annotation.SqlParam;
import com.babyfs.tk.service.biz.op.user.model.entity.RolePermissionEntity;

import java.util.List;

/**
 * 角色权限Entity Dao
 */
@Dao(entityClass = RolePermissionEntity.class)
public interface IRolePermissionDao extends IDao<RolePermissionEntity> {

    /**
     * 查询指定角色的权限
     *
     * @param roleId
     * @return
     */
    @Sql(condition = "where role_id=:roleId")
    public List<RolePermissionEntity> queryRolePermissionById(@SqlParam("roleId") long roleId);

}

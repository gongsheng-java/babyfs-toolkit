package com.babyfs.tk.service.biz.service.backend.user.dal;

import com.babyfs.tk.dal.db.IDao;
import com.babyfs.tk.dal.db.annotation.Dao;
import com.babyfs.tk.dal.db.annotation.Sql;
import com.babyfs.tk.dal.db.annotation.SqlParam;
import com.babyfs.tk.dal.db.annotation.SqlType;
import com.babyfs.tk.service.biz.service.backend.user.model.entity.RoleEntity;

import java.util.List;

/**
 * 角色Entity Dao
 */
@Dao(entityClass = RoleEntity.class)
public interface IRoleDao extends IDao<RoleEntity> {

    /**
     * 查询所有的角色
     *
     * @return
     */
    @Sql(condition = "")
    List<RoleEntity> queryAllRoles();

    /**
     * @param name
     * @return
     */
    @Sql(condition = " where name = :name")
    List<RoleEntity> queryRoleByName(@SqlParam("name") String name);

    /**
     * @param name
     * @return
     * @deprecated 仅供测试使用
     */
    @Deprecated
    @Sql(type = SqlType.DELETE, condition = " where name = :name")
    int deleteByName(@SqlParam("name") String name);
}

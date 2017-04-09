package com.babyfs.tk.service.biz.op.user.dal;

import com.babyfs.tk.dal.db.IDao;
import com.babyfs.tk.dal.db.annotation.Dao;
import com.babyfs.tk.dal.db.annotation.Sql;
import com.babyfs.tk.dal.db.annotation.SqlParam;
import com.babyfs.tk.dal.db.annotation.SqlType;
import com.babyfs.tk.service.biz.op.user.model.entity.BackendUserRoleEntity;

import java.util.List;

/**
 * 后台用户角色Entity Dao
 */
@Dao(entityClass = BackendUserRoleEntity.class)
public interface IBackendUserRoleDao extends IDao<BackendUserRoleEntity> {
    /**
     * 根据用户id和角色id查询
     *
     * @param userId
     * @param roleId
     * @return
     */
    @Sql(condition = "where backend_user_id =:userId and role_id = :roleId")
    List<BackendUserRoleEntity> queryUserRole(@SqlParam("userId") long userId, @SqlParam("roleId") long roleId);

    /**
     * 查询用户的所有角色ID
     *
     * @param userId
     * @return
     */
    @Sql(condition = "where backend_user_id =:userId")
    List<BackendUserRoleEntity> queryUserRole(@SqlParam("userId") long userId);

    /**
     * 删除指定用户的指定角色
     *
     * @param userId
     * @param roleId
     * @return
     */
    @Sql(type = SqlType.DELETE, condition = " where backend_user_id = :userId and role_id = :roleId")
    public int delete(@SqlParam("userId") long userId, @SqlParam("roleId") long roleId);

}

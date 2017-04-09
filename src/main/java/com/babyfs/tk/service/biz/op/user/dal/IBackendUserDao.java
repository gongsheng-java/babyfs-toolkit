package com.babyfs.tk.service.biz.op.user.dal;

import com.babyfs.tk.dal.db.IDao;
import com.babyfs.tk.dal.db.annotation.Dao;
import com.babyfs.tk.dal.db.annotation.Sql;
import com.babyfs.tk.dal.db.annotation.SqlParam;
import com.babyfs.tk.dal.db.annotation.SqlType;
import com.babyfs.tk.service.biz.op.user.model.entity.BackendUserEntity;

import java.util.List;

/**
 * 后台用户Entity Dao
 */
@Dao(entityClass = BackendUserEntity.class)
public interface IBackendUserDao extends IDao<BackendUserEntity> {

    /**
     * 根据name查找用户
     *
     * @param name
     * @return
     */
    @Sql(type = SqlType.QUERY_ENTITY, condition = "where name =:name")
    List<BackendUserEntity> getByName(@SqlParam("name") String name);

    /**
     * 根据 display_name 查找用户
     *
     * @param displayName
     * @return
     */
    @Sql(type = SqlType.QUERY_ENTITY, condition = "where display_name = :display_name")
    List<BackendUserEntity> getByDisplayName(@SqlParam("display_name") String displayName);

    /**
     * 删除指定的用户
     *
     * @param id
     * @return
     */
    @Sql(type = SqlType.DELETE, condition = " where id = :id")
    int deleteById(@SqlParam("id") long id);

    /**
     * 更新用户的状态
     *
     * @param id
     * @param stat
     * @return
     */
    @Sql(type = SqlType.UPDATE_COLUMNS, columns = "stat=:stat", condition = "where id= :id")
    int updateStatus(@SqlParam("id") long id, @SqlParam("stat") byte stat);

    /**
     * 修改用户密码
     *
     * @param id
     * @param password
     * @param salt
     * @return
     */
    @Sql(type = SqlType.UPDATE_COLUMNS, columns = "password=:password,salt=:salt", condition = "where id= :id")
    int updatePassword(@SqlParam("id") long id, @SqlParam("password") String password, @SqlParam("salt") String salt);
}

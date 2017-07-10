package com.babyfs.tk.dal.db.model;

import com.babyfs.tk.dal.db.IDao;
import com.babyfs.tk.dal.db.annotation.*;

import java.util.List;

/**
 */
@Dao(entityClass = User.class)
public interface IUserDao extends IDao<User> {
    @Sql(condition = "where id > :id2 and name like :name order by id desc limit :number")
    List<User> findUserByName(@SqlParam("name") String name, @SqlParam("number") int number, @SqlParam("id2") int id2);

    @Sql(type = SqlType.QUERY_COLUMNS, columns = "*", condition = "where id > :id2 and name like :name  order by id desc limit :number")
    List<Object[]> findNameByName(@SqlParam("name") String name, @SqlParam("number") int number, @SqlParam("id2") int id2);

    @Sql(type = SqlType.QUERY_COUNT, columns = "count(*)", condition = "where name = :name")
    int findUser(@SqlParam("name") String name);

    @Sql(type = SqlType.UPDATE_COLUMNS, columns = "name=:newname", condition = "where name= :name")
    int updateName(@SqlParam("name") String name, @SqlParam("newname") String newName);

    @Sql(type = SqlType.DELETE, condition = "where name= :name")
    int deleteName(@SqlParam("name") String name);

}

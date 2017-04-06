package com.babyfs.tk.dal.db.model;

import com.babyfs.tk.orm.Shard;
import com.babyfs.tk.dal.db.IDao;
import com.babyfs.tk.dal.db.annotation.Dao;
import com.babyfs.tk.dal.db.annotation.Sql;
import com.babyfs.tk.dal.db.annotation.SqlParam;
import com.babyfs.tk.dal.db.annotation.SqlType;

import java.util.List;

/**
 */
@Dao(entityClass = ShardFriend.class)
public interface IShardFriendDao extends IDao<ShardFriend> {
    @Sql(condition = "where id = :id")
    public List<ShardFriend> find(@Shard(name = "id") @SqlParam("id") long id);

    @Sql(type = SqlType.QUERY_COLUMNS, columns = "id,name,weight", condition = "where id = :id2")
    public List<Object[]> findColumns(@Shard(name = "id") @SqlParam("id2") int id2);

    @Sql(type = SqlType.QUERY_COUNT, columns = "count(id)", condition = "")
    public int findCount(@Shard(name = "id") long id);

    @Sql(type = SqlType.UPDATE_COLUMNS, columns = "name=:newname", condition = "where id= :id")
    public int updateName(@Shard(name = "id") @SqlParam("id") long id, @SqlParam("newname") String newName);

    @Sql(type = SqlType.DELETE, condition = "where id= :id")
    public int delete(@Shard(name = "id") @SqlParam("id") long id);
}

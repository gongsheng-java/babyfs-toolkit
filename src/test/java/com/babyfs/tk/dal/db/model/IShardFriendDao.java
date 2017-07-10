package com.babyfs.tk.dal.db.model;

import com.babyfs.tk.dal.db.IDao;
import com.babyfs.tk.dal.db.annotation.*;
import com.babyfs.tk.dal.meta.Shard;

import java.util.List;

/**
 */
@Dao(entityClass = ShardFriend.class)
public interface IShardFriendDao extends IDao<ShardFriend> {
    @Sql(condition = "where id = :id")
    List<ShardFriend> find(@Shard(name = "id") @SqlParam("id") long id);

    @Sql(type = SqlType.QUERY_COLUMNS, columns = "id,name,weight", condition = "where id = :id2")
    List<Object[]> findColumns(@Shard(name = "id") @SqlParam("id2") int id2);

    @Sql(type = SqlType.QUERY_COUNT, columns = "count(*)", condition = "")
    int findCount(@Shard(name = "id") long id);

    @Sql(type = SqlType.UPDATE_COLUMNS, columns = "name=:newname", condition = "where id= :id")
    int updateName(@Shard(name = "id") @SqlParam("id") long id, @SqlParam("newname") String newName);

    @Sql(type = SqlType.DELETE, condition = "where id= :id")
    int delete(@Shard(name = "id") @SqlParam("id") long id);

    @Sql(type = SqlType.UPDATE_PARTIAL_ENTITY, condition = "", includeColumns = {"name"})
    boolean updateOnlyName(@EntityParam ShardFriend entity);

    @Sql(type = SqlType.UPDATE_PARTIAL_ENTITY, condition = "", excludeColumns = {"name"})
    boolean updateExcludeName(@EntityParam ShardFriend entity);
}

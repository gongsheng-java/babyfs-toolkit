package com.babyfs.tk.dal.db;

import com.babyfs.tk.dal.orm.IListEntity;
import com.babyfs.tk.dal.db.annotation.Sql;
import com.babyfs.tk.dal.db.annotation.SqlParam;
import com.babyfs.tk.dal.db.annotation.SqlType;

import java.util.List;

/**
 * 针对列表类型数据的Dao接口，提供公用的获取列表方法
 * <p/>
 */
public interface IListDao<T extends IListEntity> extends IDao<T> {
    /**
     * 使用offset机制查询数据,通过{@link IListEntity#getTargetId()}排序和分页
     *
     * @param ownerId
     * @param from
     * @param limit
     * @return
     */
    @Sql(type = SqlType.QUERY_COLUMNS, columns = "t_id,id", condition = "where o_id = :ownerId order by t_id desc limit :from, :limit")
    List<Object[]> getByPageByTargetId(@SqlParam("ownerId") long ownerId, @SqlParam("from") long from, @SqlParam("limit") long limit);

    /**
     * 根据next_cursor获取列表,通过{@link IListEntity#getTargetId()}排序和分页
     *
     * @param ownerId
     * @return
     */
    @Sql(type = SqlType.QUERY_COLUMNS, columns = "t_id,id", condition = "where o_id= :ownerId and t_id < :cursorId order by t_id desc limit :limit")
    List<Object[]> getByNextCursorByTargetId(@SqlParam("ownerId") long ownerId, @SqlParam("limit") long limit, @SqlParam("cursorId") long cursorId);

    /**
     * 根据pre_cursor获取列表,通过{@link IListEntity#getTargetId()}排序和分页
     *
     * @param ownerId
     * @return
     */
    @Sql(type = SqlType.QUERY_COLUMNS, columns = "t_id,id", condition = "where o_id = :ownerId and t_id > :cursorId order by t_id limit :limit")
    List<Object[]> getByPreCursorByTargetId(@SqlParam("ownerId") long ownerId, @SqlParam("limit") long limit, @SqlParam("cursorId") long cursorId);

    /**
     * 使用offset机制查询数据,通过{@link IListEntity#getId()}}排序和分页
     *
     * @param ownerId
     * @param from
     * @param limit
     * @return
     */
    @Sql(type = SqlType.QUERY_COLUMNS, columns = "t_id,id", condition = "where o_id = :ownerId order by id desc limit :from, :limit")
    List<Object[]> getByPageById(@SqlParam("ownerId") long ownerId, @SqlParam("from") long from, @SqlParam("limit") long limit);

    /**
     * 根据next_cursor获取列表,通过{@link IListEntity#getId()}}排序和分页
     *
     * @param ownerId
     * @return
     */
    @Sql(type = SqlType.QUERY_COLUMNS, columns = "t_id,id", condition = "where o_id= :ownerId and id < :cursorId order by id desc limit :limit")
    List<Object[]> getByNextCursorById(@SqlParam("ownerId") long ownerId, @SqlParam("limit") long limit, @SqlParam("cursorId") long cursorId);

    /**
     * 根据pre_cursor获取列表,通过{@link IListEntity#getId()}}排序和分页
     *
     * @param ownerId
     * @return
     */
    @Sql(type = SqlType.QUERY_COLUMNS, columns = "t_id,id", condition = "where o_id = :ownerId and id > :cursorId order by id limit :limit")
    List<Object[]> getByPreCursorById(@SqlParam("ownerId") long ownerId, @SqlParam("limit") long limit, @SqlParam("cursorId") long cursorId);

    /**
     * 根据ownerId和taqrgeId查询
     *
     * @param ownerId
     * @param targetId
     * @return
     */
    @Sql(type = SqlType.QUERY_ENTITY, condition = "where o_id = :ownerId and t_id = :targetId")
    List<T> getByOwnerAndTargetId(@SqlParam("ownerId") long ownerId, @SqlParam("targetId") long targetId);

    /**
     * 根据targetId,ownerId删除
     *
     * @param ownerId
     * @param targetId
     * @return
     */
    @Sql(type = SqlType.DELETE, condition = "where o_id = :ownerId and t_id = :targetId")
    int delete(@SqlParam("ownerId") long ownerId, @SqlParam("targetId") long targetId);

    /**
     * 根据Id删除
     *
     * @param id
     * @return
     */
    @Sql(type = SqlType.DELETE, condition = "where id = :id")
    int delete(@SqlParam("id") long id);

    /**
     * 取得总数
     *
     * @param ownerId
     * @return
     */
    @Sql(type = SqlType.QUERY_COUNT, columns = "count(*)", condition = "where ownerId = :ownerId")
    int getCount(@SqlParam("ownerId") long ownerId);
}

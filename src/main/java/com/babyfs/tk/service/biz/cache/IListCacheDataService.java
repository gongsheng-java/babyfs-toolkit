package com.babyfs.tk.service.biz.cache;

import com.babyfs.tk.commons.base.Pair;

import java.io.Serializable;
import java.util.List;

/**
 * 列表cache服务接口
 *
 * @param <LIST_ID> list id的类型
 */
public interface IListCacheDataService<LIST_ID extends Serializable> {

    /**
     * 增加一条数据
     *
     * @param listId   列表id
     * @param targetId 目标id
     * @param scoreId  用于排序的id
     * @return true, 增加成功;false,增加失败
     */
    boolean add(LIST_ID listId, long targetId, long scoreId);

    /**
     * 删除列表中指定的id
     *
     * @param listId 列表id
     * @param id     id
     * @return true, 删除成功;false,删除失败
     */
    boolean delete(LIST_ID listId, long id);

    /**
     * 加载列表,如果cursor>0,则借助cursor(target_id作为cursor)获得列表数据;否则使用分页加载机制加载
     *
     * @param listId   列表id
     * @param page     页数,从1开始
     * @param pageSize 每页的记录数,>0
     * @param cursor   游标,>=0
     * @return
     */
    Pair<Long, List<Long>> loadList(LIST_ID listId, int page, int pageSize, long cursor);

    /**
     * 加载列表id及score id,如果cursor>0,则借助cursor(scored_id作为cursor)获得列表数据;否则使用分页加载机制加载
     *
     * @param listId   列表id
     * @param page     页数,从1开始
     * @param pageSize 每页的记录数,>0
     * @param cursor   游标,>=0
     * @return {@link Pair#first}是target id,{@link Pair#second}是score id
     */
    Pair<Long, List<Pair<Long, Long>>> loadListWithScoreId(LIST_ID listId, int page, int pageSize, long cursor);
}

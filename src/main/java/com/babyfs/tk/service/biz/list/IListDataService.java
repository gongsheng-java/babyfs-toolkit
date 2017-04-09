package com.babyfs.tk.service.biz.list;

import com.babyfs.tk.commons.base.Pair;
import com.babyfs.tk.service.biz.base.entity.list.BaseListCounterEntity;
import com.babyfs.tk.service.biz.base.entity.list.BaseListEntity;

import java.util.List;

/**
 * 列表数据服务
 *
 * @param <L> 列表实体的类型
 * @param <E> 列表计数实体的类型
 */
public interface IListDataService<L extends BaseListEntity, E extends BaseListCounterEntity> {

    /**
     * 增加列表一个实体
     *
     * @param entity entity
     * @return 操作结果
     */
    L add(L entity);

    /**
     * 删除一个列表实体
     *
     * @param ownerId  属主的id
     * @param targetId 目标的id
     * @return 操作是否成功
     */
    boolean del(long ownerId, long targetId);

    /**
     * 根据ownerId 和 targetId查询{@link L#getId()}
     *
     * @param ownerId
     * @param targetId
     * @return
     */
    Long getOwnerTargetId(long ownerId, long targetId);

    /**
     * 获取总数
     *
     * @param ownerId 列表id
     * @return
     */
    long getCount(long ownerId);

    /**
     * 加载列表,如果cursor>0,则借助cursor(target_id作为cursor)获得列表数据;否则使用分页加载机制加载
     *
     * @param ownerId  列表id
     * @param page     页数,从1开始
     * @param pageSize 每页的记录数,>0
     * @param cursor   游标,>=0
     * @return target id
     */
    Pair<Long, List<Long>> loadList(long ownerId, int page, int pageSize, long cursor);

    /**
     * 加载列表id及score id,如果cursor>0,则借助cursor(scored_id作为cursor)获得列表数据;否则使用分页加载机制加载
     *
     * @param ownerId  列表id
     * @param page     页数,从1开始
     * @param pageSize 每页的记录数,>0
     * @param cursor   游标,>=0
     * @return {@link Pair#first}是target id,{@link Pair#second}是score id
     */
    Pair<Long, List<Pair<Long, Long>>> loadListWithScoreId(long ownerId, int page, int pageSize, long cursor);
}

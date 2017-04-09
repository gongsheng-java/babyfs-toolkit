package com.babyfs.tk.dal.orm;

/**
 * 一种列表类型的通用数据接口
 * <p/>
 * 其属性主要包括 ownerId(持有者Id) , targetId(持有的目标Id) , createdTime(创建时间)
 * 使用时主要是按时间序进行分页列表查询
 * <p/>
 * 使用场合：
 * <p/>
 * 用户自己的消息列表：ownerId ＝ userId , targetId = msgId
 * 用户的聚合消息列表：同上
 * 用户的关注列表：   ownerId = userId , targetId = followingUserId
 * 用户的粉丝列表：   ownerId = userId , targetId = followedUserId
 * <p/>
 * <p/>
 */
public interface IListEntity extends IEntity {

    /**
     * 获得持有者ID
     *
     * @return
     */
    long getOwnerId();

    /**
     * 获得持有目标ID
     *
     * @return
     */
    long getTargetId();

    /**
     * 获得创建时间
     *
     * @return
     */
    long getTimestamp();
}

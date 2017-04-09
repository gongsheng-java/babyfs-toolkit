package com.babyfs.tk.service.biz.base.model;

import com.babyfs.tk.dal.orm.IEntity;
import com.google.common.base.Preconditions;

/**
 * 实体事件
 *
 * @param <T> 实体的类型
 * @param <A> 附加数据的类型
 */
public class EntityEvent<T extends IEntity, A> implements IEvent {
    /**
     * 实体的ID
     */
    private final long id;
    /**
     * 变更类型
     */
    private final ChangeType changeType;
    /**
     * 实体数据
     */
    private final T entity;
    /**
     * 附加的数据
     */
    private final A attach;
    /**
     * 对于ES,是否将update操作作为add,用于更新索引
     */
    private boolean updateAsAddForES;

    /**
     * 默认{@link #updateAsAddForES}为true
     *
     * @param changeType not null
     * @param id         >0
     */
    public EntityEvent(ChangeType changeType, long id) {
        this(changeType, id, null, null);
    }

    /**
     * 默认{@link #updateAsAddForES}为true
     *
     * @param changeType not null
     * @param id         >0
     * @param entity     nullable
     * @param attach     nullable
     */
    public EntityEvent(ChangeType changeType, long id, T entity, A attach) {
        this(changeType, id, entity, attach, true);
    }

    /**
     * @param changeType
     * @param id
     * @param entity
     * @param attach
     * @param updateAsAddForES 对于ES,是否将update事件视为add
     */
    public EntityEvent(ChangeType changeType, long id, T entity, A attach, boolean updateAsAddForES) {
        this.changeType = Preconditions.checkNotNull(changeType);
        this.id = id;
        this.entity = entity;
        this.attach = attach;
        this.updateAsAddForES = updateAsAddForES;
    }


    public long getId() {
        return id;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public T getEntity() {
        return entity;
    }

    public A getAttach() {
        return attach;
    }

    public boolean isUpdateAsAddForES() {
        return updateAsAddForES;
    }

    public void setUpdateAsAddForES(boolean updateAsAddForES) {
        this.updateAsAddForES = updateAsAddForES;
    }

    @Override
    public String toString() {
        return "EntityEvent{" +
                "id=" + id +
                ", changeType=" + changeType +
                ", entity=" + (entity != null ? entity.getClass() : null) +
                ", updateAsAddForES=" + updateAsAddForES +
                '}';
    }
}

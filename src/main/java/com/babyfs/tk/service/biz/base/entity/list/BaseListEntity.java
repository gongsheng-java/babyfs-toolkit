package com.babyfs.tk.service.biz.base.entity.list;

import com.babyfs.tk.dal.orm.AutoIdEntity;
import com.babyfs.tk.dal.orm.IListEntity;

import javax.persistence.Column;

/**
 * 列表
 */
public abstract class BaseListEntity extends AutoIdEntity implements IListEntity {
    private static final long serialVersionUID = 5080832269485442716L;
    /**
     * 属主ID
     */
    private long ownerId;
    /**
     * 对象ID
     */
    private long targetId;
    /**
     * 时间戳
     */
    private long timestamp;

    public BaseListEntity() {
    }

    public BaseListEntity(long ownerId, long targetId, long timestamp) {
        this.ownerId = ownerId;
        this.targetId = targetId;
        this.timestamp = timestamp;
    }

    @Column(name = "o_id")
    public long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(long ownerId) {
        this.ownerId = ownerId;
    }

    @Column(name = "t_id")
    public long getTargetId() {
        return targetId;
    }

    public void setTargetId(long targetId) {
        this.targetId = targetId;
    }

    @Column(name = "ts")
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "BaseListEntity{" +
                "ownerId=" + ownerId +
                ", targetId=" + targetId +
                ", timestamp=" + timestamp +
                '}';
    }
}

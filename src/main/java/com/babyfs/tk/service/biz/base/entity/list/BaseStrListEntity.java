package com.babyfs.tk.service.biz.base.entity.list;

import com.babyfs.tk.dal.orm.AutoIdEntity;

import javax.persistence.Column;

/**
 * 使用String作为ownerId的列表
 */
public abstract class BaseStrListEntity extends AutoIdEntity {
    private static final long serialVersionUID = -6681892137280391977L;
    /**
     * 属主ID
     */
    private String ownerId;
    /**
     * 对象ID
     */
    private long targetId;
    /**
     * 时间戳
     */
    private long timestamp;

    public BaseStrListEntity() {
    }

    public BaseStrListEntity(String ownerId, long targetId, long timestamp) {
        this.ownerId = ownerId;
        this.targetId = targetId;
        this.timestamp = timestamp;
    }

    @Column(name = "o_id")
    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
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

package com.babyfs.tk.service.biz.base.entity.list;


import com.babyfs.tk.dal.orm.AssignIdEntity;

import javax.persistence.Column;

/**
 * 列表计数,{@link #getId()}设置为{@link BaseListEntity#getOwnerId()}
 */
public abstract class BaseListCounterEntity extends AssignIdEntity {
    private static final long serialVersionUID = 5758696543252739813L;
    /**
     * 计数
     */
    private long counter;

    public BaseListCounterEntity() {
    }


    @Column(name = "counter")
    public long getCounter() {
        return counter;
    }

    public void setCounter(long counter) {
        this.counter = counter;
    }
}

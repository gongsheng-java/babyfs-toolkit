package com.babyfs.tk.service.biz.base.entity.list;


import com.babyfs.tk.dal.orm.AutoIdEntity;

import javax.persistence.Column;

/**
 * owner id 为String的列表计数
 */
public abstract class BaseStrListCounterEntity extends AutoIdEntity {
    private static final long serialVersionUID = -3311474250427374435L;
    /**
     * @see BaseStrListEntity#getOwnerId()
     */
    private String onwerId;
    /**
     * 计数
     */
    private long counter;

    public BaseStrListCounterEntity() {
    }

    @Column(name = "o_id")
    public String getOnwerId() {
        return onwerId;
    }

    public void setOnwerId(String onwerId) {
        this.onwerId = onwerId;
    }

    @Column(name = "counter")
    public long getCounter() {
        return counter;
    }

    public void setCounter(long counter) {
        this.counter = counter;
    }
}

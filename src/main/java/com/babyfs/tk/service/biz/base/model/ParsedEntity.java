package com.babyfs.tk.service.biz.base.model;

import com.babyfs.tk.dal.orm.IEntity;

/**
 * 带有解析数据的实体
 */
public class ParsedEntity<E extends IEntity> {
    /**
     * 原实体
     */
    private transient E entity;
    /**
     * 解析后的数据,一般是从实体的conf字段解析
     */
    private transient Object parsed;

    public E getEntity() {
        return entity;
    }

    public void setEntity(E entity) {
        this.entity = entity;
    }

    @SuppressWarnings("unchecked")
    public <T> T getParsed() {
        return (T) parsed;
    }

    public void setParsed(Object parsed) {
        this.parsed = parsed;
    }
}

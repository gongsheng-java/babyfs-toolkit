package com.babyfs.tk.service.biz.base.model;

import com.babyfs.tk.dal.orm.IEntity;

/**
 * 带有解析数据的实体
 * @param <E> 实体的类型
 * @param <T> 实体字段解析后的类型
 */
public class ParsedEntity<E extends IEntity, T> {
    /**
     * 原实体
     */
    private E entity;
    /**
     * 解析后的数据,一般是从实体的conf字段解析
     */
    private T parsed;

    public E getEntity() {
        return entity;
    }

    public void setEntity(E entity) {
        this.entity = entity;
    }

    public T getParsed() {
        return parsed;
    }

    public void setParsed(T parsed) {
        this.parsed = parsed;
    }
}

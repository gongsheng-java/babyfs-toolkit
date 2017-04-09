package com.babyfs.tk.service.biz.kvconf.model;


import com.babyfs.tk.service.biz.base.query.BaseQuery;

/**
 * {@link KVConfEntity}的查询参数
 */
public class KVConfQuery extends BaseQuery {
    /**
     * 名称
     */
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

package com.babyfs.tk.service.biz.op.user.model;

import com.babyfs.servicetk.apicore.rbac.ResourceV2;

import java.io.Serializable;

/**
 * 简单的resource
 */
public class PlainResource implements ResourceV2,Serializable{
    private static final long serialVersionUID = -1966026580343775508L;
    /**
     * 取得资源的名称
     *
     * @return
     */
    private String name;

    /**
     * 取得资源的ID
     *
     * @return
     */
    private String id;

    /**
     * 取得资源的类型
     *
     * @return
     */
    private int type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}

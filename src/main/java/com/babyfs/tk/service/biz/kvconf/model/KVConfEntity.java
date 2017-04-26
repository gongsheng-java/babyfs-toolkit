package com.babyfs.tk.service.biz.kvconf.model;


import com.babyfs.tk.service.biz.base.entity.BaseAutoIdEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * 简单的Key Value配置
 */
@Entity
@Table(name = "t_kv_conf")
public class KVConfEntity extends BaseAutoIdEntity {
    private static final long serialVersionUID = 2502347751460671413L;
    /**
     * 配置的名称
     */
    private String name;
    /**
     * 配置类型
     */
    private int type;
    /**
     * 配置的内容
     */
    private String content;

    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "type")
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Column(name = "content")
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

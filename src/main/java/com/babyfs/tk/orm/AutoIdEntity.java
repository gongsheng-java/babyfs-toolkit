package com.babyfs.tk.orm;

import javax.persistence.*;

/**
 * 由数据库自动生成ID的实体基类
 * <p/>
 */
@Entity
public abstract class AutoIdEntity implements IEntity {
    private long id;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    public final long getId() {
        return id;
    }

    public final void setId(long id) {
        if (id < 0) {
            throw new IllegalArgumentException("id must >= 0");
        }
        this.id = id;
    }
}

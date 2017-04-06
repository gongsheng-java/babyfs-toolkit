package com.babyfs.tk.orm;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * 指定ID的实体基类
 * <p/>
 */
@Entity
public abstract class AssignIdEntity implements IEntity {
    private long id;

    @Id
    @Column(name = "id")
    public final long getId() {
        return id;
    }

    public final void setId(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("id must >0");
        }
        this.id = id;
    }
}

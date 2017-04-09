package com.babyfs.tk.dal.orm;

import com.babyfs.tk.dal.meta.Shard;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * 指定ID的实体基类,使用id进行分库分表
 * <p/>
 */
@Entity
public abstract class AssignIdShardEntity implements IEntity {
    private long id;

    @Id
    @Column(name = "id")
    @Shard(name = "id")
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

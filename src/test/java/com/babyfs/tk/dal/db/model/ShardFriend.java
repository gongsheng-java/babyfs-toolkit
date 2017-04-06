package com.babyfs.tk.dal.db.model;

import com.babyfs.tk.orm.AssignIdShardEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 分库分表的Friend
 */
@Entity
@Table(name = "friend_shard")
public class ShardFriend extends AssignIdShardEntity {
    private String name;
    private int weight;
    private int height;

    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "weight")
    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    @Column(name = "height")
    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}

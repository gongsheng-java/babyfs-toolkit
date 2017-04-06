package com.babyfs.tk.dal.db.model;

import com.babyfs.tk.orm.AssignIdEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * <pre>
 * CREATE TABLE friend(id int not null primary key,name char(20),weight int,height int);
 *
 * );
 * </pre>
 */
@Entity
@Table(name = "friend")
public class Friend extends AssignIdEntity {
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

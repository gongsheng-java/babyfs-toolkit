package com.babyfs.tk.dal.db.model;

import com.babyfs.tk.orm.AutoIdEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * CREATE TABLE user(id int not null primary key,name char(20));
 */
@Entity
@Table(name = "user")
public class User extends AutoIdEntity {
    private String name;

    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("User");
        sb.append("{id='").append(getId()).append('\'');
        sb.append("name='").append(name).append('\'');
        sb.append('}');
        return sb.toString();
    }
}

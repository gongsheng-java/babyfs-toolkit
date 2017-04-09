package com.babyfs.tk.dal.db.model;

import com.babyfs.tk.dal.orm.AssignIdEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * <pre>
 *     CREATE TABLE counter(id int not null primary key,counter int);
 * </pre>
 */
@Entity
@Table(name = "counter")
public class Counter extends AssignIdEntity {
    private long counter;

    @Column(name = "counter")
    public long getCounter() {
        return counter;
    }

    public void setCounter(long counter) {
        this.counter = counter;
    }
}

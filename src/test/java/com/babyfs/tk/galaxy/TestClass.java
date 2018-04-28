package com.babyfs.tk.galaxy;

import java.io.Serializable;
import java.sql.Date;

public class TestClass implements Serializable {
    private String name;
    private Date date;
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}

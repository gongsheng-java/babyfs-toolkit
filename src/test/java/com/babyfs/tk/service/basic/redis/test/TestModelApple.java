package com.babyfs.tk.service.basic.redis.test;

import java.io.Serializable;

/**
 */
public class TestModelApple implements Serializable{
    private static final long serialVersionUID = 1896394648488390533L;
    private String str ;
    private int in;
    private Long l;
    private float  f;

    public TestModelApple() {
        this.str = "apple";
        this.in = 1;
        this.l = 2l;
        this.f =  1.5f;
    }

    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }

    public int getIn() {
        return in;
    }

    public void setIn(int in) {
        this.in = in;
    }

    public Long getL() {
        return l;
    }

    public void setL(Long l) {
        this.l = l;
    }

    public float getF() {
        return f;
    }

    public void setF(float f) {
        this.f = f;
    }
}

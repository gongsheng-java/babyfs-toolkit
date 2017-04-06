package com.babyfs.tk.service.biz.service.parambean.internal;


import com.babyfs.tk.service.biz.service.parambean.annotation.ParamMetaData;

/**
 */
public abstract class TestBean {
    private Integer i;

    private String my;

    @ParamMetaData(paramName = "i", rule = "requiredId")
    public void setInteger(Integer i){
        this.i = i;
    }

    public Integer getInteger() {
        return i;
    }

    @ParamMetaData(paramName = "s", rule = "notEmpty")
    abstract void setString(String s);

    abstract String getString();

    public String getMy() {
        return my;
    }

    public void setMy(String my) {
        this.my = my;
    }
}

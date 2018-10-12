package com.babyfs.tk.commons.model;

/*
* 请求基类
* */
public class ServiceRequest {
    private Operator operator;

    public ServiceRequest() {
        //todo:需要增加operator初始化机制，从web底层穿透到service层
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }
}

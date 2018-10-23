package com.babyfs.tk.commons.model;

import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Inject;

/*
* 请求基类
* */
public class ServiceRequest {
    private Operator operator;

    public ServiceRequest() {
        //增加operator初始化机制，从web底层穿透到service层
        Object cache = RequestContextCache.get(CacheConst.REQUEST_OPERATER);
        if(cache != null) {
            this.operator = new Operator();
            operator.setName(cache.toString());
        }
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }
}

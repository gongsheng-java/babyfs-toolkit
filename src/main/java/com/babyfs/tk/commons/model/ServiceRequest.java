package com.babyfs.tk.commons.model;

import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Inject;

/*
* 请求基类
* */
public class ServiceRequest {
    private Operator operator;

    public ServiceRequest() {
        this.operator = new Operator();
        //增加operator初始化机制，从web底层穿透到service层
        Object cache = RequestContextCache.get(CacheConst.REQUEST_OPERATER);
        if(cache != null) {
            operator.setName(cache.toString());
        }

        Object ipCache = RequestContextCache.get(CacheConst.REQUEST_IP);
        if(ipCache != null) {
            operator.setIp(ipCache.toString());
        }
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }
}

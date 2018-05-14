package com.babyfs.tk.service.biz.op.user.model;

import java.io.Serializable;

/**
 * 简单的实现
 */
public class PlainPermission implements Permission, Serializable {
    private static final long serialVersionUID = 4481164808977035060L;

    private long id;
    private PlainResource target;
    private PlainOperation operation;


    public PlainPermission() {
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public Resource getTarget() {
        return target;
    }

    @Override
    public IOperation getOperation() {
        return operation;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setTarget(PlainResource target) {
        this.target = target;
    }

    public void setOperation(PlainOperation operation) {
        this.operation = operation;
    }
}

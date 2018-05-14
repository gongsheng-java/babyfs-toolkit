package com.babyfs.tk.service.biz.op.user.model;

import java.io.Serializable;

/**
 * {@link IOperation}的简单实现
 */
public class PlainOperation implements IOperation, Serializable {
    private static final long serialVersionUID = -6581829904573440836L;
    private int mask;

    @Override
    public int getMask() {
        return mask;
    }

    public void setMask(int mask) {
        this.mask = mask;
    }
}

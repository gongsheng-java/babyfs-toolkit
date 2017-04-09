package com.babyfs.tk.service.biz.op.user.model;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;

/**
 * 简单的实现
 */
public class SimplePermission implements Permission {
    private final long id;
    private final Resource target;
    private final Operation operation;

    /**
     * @param target
     * @param operation
     */
    public SimplePermission(@Nonnull Resource target, @Nonnull Operation operation, long id) {
        this.target = Preconditions.checkNotNull(target);
        this.operation = Preconditions.checkNotNull(operation);
        this.id = id;
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
    public Operation getOperation() {
        return operation;
    }
}

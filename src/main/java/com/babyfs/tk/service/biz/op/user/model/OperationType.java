package com.babyfs.tk.service.biz.op.user.model;

/**
 * 操作的类型
 */
public enum OperationType implements IOperation {
    READ(1),
    INSERT(1 << 1),
    UPDATE(1 << 2),
    DELETE(1 << 3),
    ALL(READ.mask | INSERT.mask | UPDATE.mask | DELETE.mask);

    private final int mask;

    public int getMask() {
        return mask;
    }

    OperationType(int mask) {
        this.mask = mask;
    }
}

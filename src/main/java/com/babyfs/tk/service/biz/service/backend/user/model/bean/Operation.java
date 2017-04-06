package com.babyfs.tk.service.biz.service.backend.user.model.bean;

import com.google.common.base.Preconditions;

/**
 * 操作
 */
public final class Operation implements IOperation {
    public static final Operation READ = createOperation(OperationType.READ);
    public static final Operation INSERT = createOperation(OperationType.INSERT);
    public static final Operation UPDATE = createOperation(OperationType.UPDATE);
    public static final Operation DELETE = createOperation(OperationType.DELETE);
    public static final int ALL = OperationType.ALL.getMask();

    private final int mask;

    /**
     * 操作的掩码
     */
    private Operation(int mask) {
        this.mask = mask;
    }

    /**
     * 构建Opertaion
     *
     * @param operations
     * @return
     */
    public static Operation createOperation(IOperation... operations) {
        Preconditions.checkArgument(operations != null && operations.length > 0);
        int mask = 0;
        for (IOperation operation : operations) {
            mask |= operation.getMask();
        }
        return new Operation(mask);
    }

    /**
     * 使用掩码构建Operation
     *
     * @param mask
     * @return
     */
    public static Operation createOperation(int mask) {
        Preconditions.checkArgument(mask > 0 && mask <= ALL);
        return new Operation(mask);
    }

    @Override
    public int getMask() {
        return mask;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Operation operation = (Operation) o;

        if (mask != operation.mask) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return mask;
    }
}

package com.babyfs.tk.service.biz.op.user.model;

/**
 * 资源的类型
 */
public enum ResourceType {
    BIZ_RESOURCE(1);

    private final int value;

    private ResourceType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

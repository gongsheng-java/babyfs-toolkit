package com.babyfs.tk.service.biz.service.backend.user;

/**
 * 状态
 */
public enum Status {
    /**
     * 禁用
     */
    DISABLE((byte) 0),
    /**
     * 启用
     */
    ENABLE((byte) 1);
    private final byte value;

    Status(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }

    /**
     * @param value
     * @return
     */
    public static Status getStatusByValue(final byte value) {
        switch (value) {
            case 0:
                return DISABLE;
            case 1:
                return ENABLE;
            default:
                return null;
        }
    }
}

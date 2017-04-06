package com.babyfs.tk.commons.enums;

/**
 * 标准Boolean枚举
 */
public enum BooleanEnum {

    ALL((byte) 0, true),
    FALSE((byte) 1, false),
    TRUE((byte) 2, true);

    private byte value;
    private Boolean boolValue;

    private BooleanEnum(byte value, Boolean boolValue) {
        this.value = value;
        this.boolValue = boolValue;
    }

    public byte getValue() {
        return this.value;
    }

    public Boolean getBoolValue() {
        return this.boolValue;
    }

    public static BooleanEnum valueOf(byte value) {
        if (value == BooleanEnum.FALSE.getValue()) {
            return BooleanEnum.FALSE;
        } else if (value == BooleanEnum.TRUE.getValue()) {
            return BooleanEnum.TRUE;
        } else if (value == BooleanEnum.ALL.getValue()) {
            return BooleanEnum.ALL;
        }
        return null;
    }

    public static BooleanEnum valueOf(boolean value) {
        if (value) {
            return BooleanEnum.TRUE;
        } else  {
            return BooleanEnum.FALSE;
        }
    }

    public static boolean isEquals(byte value, BooleanEnum booleanEnum) {
        return booleanEnum.getValue() == value;
    }

}

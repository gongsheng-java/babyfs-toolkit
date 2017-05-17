package com.babyfs.tk.commons.enums;

/**
 * 软删除标识
 * <p/>
 */
public enum DeleteEnum {
    /**
     * 正常
     */
    NORMAL((byte) 0),
    /**
     * 已删除
     */
    DELETED((byte) 1);

    /**
     * 值
     */
    private final byte val;

    DeleteEnum(byte val) {
        this.val = val;
    }

    public byte getVal() {
        return val;
    }

    /**
     * 根据val值取得{@link DeleteEnum}
     *
     * @param val val
     * @return 如果val值无效;则返回null
     */
    public static DeleteEnum valueOf(byte val) {
        if (val == 0) {
            return NORMAL;
        } else if (val == 1) {
            return DELETED;
        } else {
            return null;
        }
    }

    /**
     * 是否是正常的
     *
     * @param value
     * @return
     */
    public static boolean isNormal(int value) {
        return value == NORMAL.getVal();
    }

    /**
     * 是否已经删除
     *
     * @param value
     * @return
     */
    public static boolean isDeleted(int value) {
        return value == DELETED.getVal();
    }
}

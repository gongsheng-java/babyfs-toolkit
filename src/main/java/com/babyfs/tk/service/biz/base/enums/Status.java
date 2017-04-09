package com.babyfs.tk.service.biz.base.enums;


import com.babyfs.tk.commons.enums.IndexedEnum;

import java.util.List;

/**
 * 状态类型
 */
public enum Status implements IndexedEnum {
    /**
     * 无效/false
     */
    DISABLE((byte) 0),
    /**
     * 有效/true
     */
    ENABLE((byte) 1);

    private static final List<Status> INDEXS = IndexedEnum.Util.toIndexes(Status.values());

    private byte index;

    Status(byte index) {
        this.index = index;
    }

    @Override
    public int getIndex() {
        return index;
    }

    public byte getValue() {
        return index;
    }

    /**
     * 根据index 获取指定的枚举
     *
     * @param index
     * @return
     */
    public static Status indexOf(final int index) {
        return IndexedEnum.Util.valueOf(INDEXS, index);
    }
}

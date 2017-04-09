package com.babyfs.tk.service.biz.base.model;


import com.babyfs.tk.commons.enums.IndexedEnum;

import java.util.List;

/**
 * 事件变更的类型
 */
public enum ChangeType implements IndexedEnum {
    /**
     * 新增
     */
    ADD(0),
    /**
     * 更新
     */
    UPDATE(1),
    /**
     * 删除
     */
    DEL(2);

    private static final List<ChangeType> INDEXS = Util.toIndexes(ChangeType.values());

    private final int index;

    ChangeType(int index) {
        this.index = index;
    }

    @Override
    public int getIndex() {
        return index;
    }


    /**
     * 根据index 获取指定的枚举
     *
     * @param index
     * @return
     */
    public static ChangeType indexOf(final int index) {
        return Util.valueOf(INDEXS, index);
    }
}

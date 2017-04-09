package com.babyfs.tk.service.biz.sensitive;


import com.babyfs.tk.commons.enums.IndexedEnum;

import java.util.List;

/**
 * 敏感词的类型
 */
public enum SensitiveWordsType implements IndexedEnum {
    /**
     * 禁止发布的敏感词
     */
    FORIBIDDEN(1, "禁止发布"),
    /**
     * 过滤的敏感词
     */
    FILTER(2, "过滤");

    private static final List<SensitiveWordsType> INDEXS = Util.toIndexes(SensitiveWordsType.values());

    private final int index;
    private final String name;

    SensitiveWordsType(int index, String name) {
        this.index = index;
        this.name = name;
    }


    @Override
    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    /**
     * 根据index 获取指定的枚举
     *
     * @param index
     * @return
     */
    public static SensitiveWordsType indexOf(final int index) {
        return Util.valueOf(INDEXS, index);
    }
}
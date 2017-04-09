package com.babyfs.tk.service.biz.kvconf;


import com.babyfs.tk.commons.enums.IndexedEnum;

import java.util.List;
import com.babyfs.tk.service.biz.kvconf.model.KVConfEntity;

/**
 * {@link KVConfEntity#getType()}类型定义
 */
public enum KVConfType implements IndexedEnum {
    /**
     * 普通文本
     */
    TEXT(0),
    /**
     * 整数
     */
    INTEGER(1),
    /**
     * 小数
     */
    DOUBLE(2),
    /**
     * JSON格式文本,{@link com.alibaba.fastjson.JSONObject}
     */
    JSONOBJECT_TEXT(3);

    private static final List<KVConfType> INDEXS = Util.toIndexes(KVConfType.values());

    private final int index;

    KVConfType(int index) {
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
    public static KVConfType indexOf(final int index) {
        return Util.valueOf(INDEXS, index);
    }
}
package com.babyfs.tk.service.biz.serialnum.enums;

import com.babyfs.tk.commons.enums.IndexedEnum;

import java.util.List;

public enum SerialNumType implements IndexedEnum {
    FISSION_TRANSACTION(1, "分销交易"),
    RETAILERS_ORDER(2, "商城订单");

    private static final List<SerialNumType> INDEXS = Util.toIndexes(SerialNumType.values());

    private final int index;
    private final String name;

    SerialNumType(int index, String name) {
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

    public static SerialNumType indexOf(final int index) {
        return Util.valueOf(INDEXS, index);
    }
}

package com.babyfs.tk.service.biz.cache.utils;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

/**
 * DAL根据 SqlType QUERY_COLUMNS查询时候，返回的是 Object[]
 *
 * 这个Func专门将查询 id 时的结果转化为 Long
 *
 */
public class ObjArray2IdFunction implements Function<Object[], Long> {

    public static final ObjArray2IdFunction INSTANCE = new ObjArray2IdFunction();

    @Override
    public Long apply(Object[] input) {
        Preconditions.checkArgument(input != null);
        return Long.parseLong(input[0].toString());
    }

}

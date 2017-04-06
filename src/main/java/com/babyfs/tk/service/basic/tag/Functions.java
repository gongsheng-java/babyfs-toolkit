package com.babyfs.tk.service.basic.tag;


import com.alibaba.fastjson.serializer.SerializeFilter;
import com.google.common.collect.ImmutableList;
import com.babyfs.tk.service.basic.utils.JSONUtil;

/**
 * JSP Tag Functions
 */
public final class Functions {
    private static final ImmutableList<SerializeFilter> ENTITY_FILTERS = ImmutableList.<SerializeFilter>builder()
            .add().build();

    private Functions() {

    }

    /**
     * 将对象转换为JSON字符串
     *
     * @param object
     * @return
     */
    public static String toJSON(Object object) {
        if (object == null) {
            return "{}";
        } else {
            return JSONUtil.toJSONString(object, ENTITY_FILTERS);
        }
    }
}

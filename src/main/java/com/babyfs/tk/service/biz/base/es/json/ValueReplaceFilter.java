package com.babyfs.tk.service.biz.base.es.json;

import com.alibaba.fastjson.serializer.ValueFilter;
import com.google.common.base.Preconditions;
import com.babyfs.tk.commons.base.Tuple;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

/**
 * 值修改
 */
public class ValueReplaceFilter<T> implements ValueFilter {
    private final Class<T> clazz;
    private final Map<String, Function<Tuple<T, String, Object>, Object>> attributeValueFuncs;

    /**
     * @param clazz               not null
     * @param attributeValueFuncs not null,属性修改函数,key为属性名,value是修改函数,函数的参数格式符合{@link ValueFilter#process(Object, String, Object)}一致
     */
    public ValueReplaceFilter(Class<T> clazz, Map<String, Function<Tuple<T, String, Object>, Object>> attributeValueFuncs) {
        this.clazz = Preconditions.checkNotNull(clazz);
        if (attributeValueFuncs == null) {
            this.attributeValueFuncs = Collections.emptyMap();
        } else {
            this.attributeValueFuncs = attributeValueFuncs;
        }
    }

    @Override
    public Object process(Object object, String name, Object value) {
        if (!clazz.isInstance(object)) {
            return value;
        }

        Function<Tuple<T, String, Object>, Object> function = this.attributeValueFuncs.get(name);

        if (function != null) {
            return function.apply(Tuple.of((T) object, name, value));
        } else {
            return value;
        }
    }
}

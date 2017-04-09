package com.babyfs.tk.service.biz.base.es.json;

import com.alibaba.fastjson.serializer.AfterFilter;
import com.google.common.base.Preconditions;
import com.babyfs.tk.commons.base.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Function;

/**
 * 增加值
 */
public class ValueAddAfterFilter<T> extends AfterFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ValueAddAfterFilter.class);
    private final Class<T> clazz;
    private final Function<T, List<Pair<String, Object>>> func;

    /**
     * @param clazz not null
     * @param func  not null,属性增加函数,func的参数是clazz的实例,返回结果是需要新增加的属性列表
     *              func的返回值如果都是简单属性,可以一次返回多个简单属性;
     *              如果返回值是复合对象,则只返回一个值,即复合对象,应该有单独的ValueAddAfterFiler,否则会导致fastjson序列化时抛出NPE
     */
    public ValueAddAfterFilter(Class<T> clazz, Function<T, List<Pair<String, Object>>> func) {
        this.clazz = Preconditions.checkNotNull(clazz);
        this.func = Preconditions.checkNotNull(func);
    }

    @Override
    public void writeAfter(Object object) {
        if (!clazz.isInstance(object)) {
            return;
        }

        @SuppressWarnings("unchecked")
        List<Pair<String, Object>> result = this.func.apply((T) object);
        if (result == null || result.isEmpty()) {
            return;
        }


        //增加值
        for (Pair<String, Object> pair : result) {
            try {
                writeKeyValue(pair.first, pair.second);
            } catch (Exception e) {
                LOGGER.error("write key value fail,object:" + object + ",key:" + pair.first + ",value:" + pair.second, e);
                throw e;
            }
        }
    }
}

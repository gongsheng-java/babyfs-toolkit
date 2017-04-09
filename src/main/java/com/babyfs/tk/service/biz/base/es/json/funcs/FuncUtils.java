package com.babyfs.tk.service.biz.base.es.json.funcs;

import com.google.common.base.Preconditions;
import com.babyfs.tk.commons.base.Tuple;

import java.util.Map;
import java.util.function.Function;

/**
 * 函数工具类
 */
public class FuncUtils {
    /**
     * 添加`值`替换的函数
     *
     * @param name  属性名称,not null
     * @param value 值
     * @param map   map,not null
     * @param <T>   对象类型
     * @param <V>   name对应的新属性类型
     */
    public static <T, V> void addReplaceValueFunc(String name, V value, Map<String, Function<Tuple<T, String, Object>, Object>> map) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(map);
        map.put(name, new ReplaceValueFunction<T, Object>(value));
    }

}

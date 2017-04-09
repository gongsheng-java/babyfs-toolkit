package com.babyfs.tk.service.biz.base.es.json.funcs;

import com.babyfs.tk.commons.base.Tuple;

import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * @param <T>
 * @param <V>
 */
public class ReplaceValueFunction<T, V> implements Function<Tuple<T, String, Object>, Object> {
    private final V value;

    public ReplaceValueFunction(V value) {
        this.value = value;
    }

    @Nullable
    @Override
    public Object apply(@Nullable Tuple<T, String, Object> input) {
        return value;
    }
}

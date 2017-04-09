package com.babyfs.tk.service.biz.base.es.json.funcs;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.babyfs.tk.commons.base.Pair;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

/**
 * @param <T>
 */
public class AfterValueFunction<T> implements Function<T, List<Pair<String, Object>>> {
    private final List<Pair<String, Object>> values = Lists.newArrayList();

    public AfterValueFunction() {
    }

    /**
     * @param name
     * @param value
     */
    public AfterValueFunction(String name, Object value) {
        Preconditions.checkNotNull(name);
        this.values.add(Pair.of(name, value));
    }

    public AfterValueFunction<T> add(String name, Object value) {
        this.values.add(Pair.of(name, value));
        return this;
    }

    @Nullable
    @Override
    public List<Pair<String, Object>> apply(@Nullable T input) {
        return this.values;
    }
}

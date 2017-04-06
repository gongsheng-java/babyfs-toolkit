package com.babyfs.tk.dal.db.funcs;

import com.google.common.base.Function;
import com.babyfs.tk.commons.base.Pair;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * 设置Shard参数的Function
 */
public final class SetShardParameterFunc implements Function<Pair<Map<String, Object>, Object[]>, Void> {
    private final int parameterIndex;
    private final String name;

    public SetShardParameterFunc(int parameterIndex, String name) {
        this.parameterIndex = parameterIndex;
        this.name = name;
    }

    @Override
    public Void apply(@Nonnull Pair<Map<String, Object>, Object[]> input) {
        input.first.put(name, input.second[parameterIndex]);
        return null;
    }
}

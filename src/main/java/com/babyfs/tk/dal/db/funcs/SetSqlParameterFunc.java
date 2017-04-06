package com.babyfs.tk.dal.db.funcs;

import com.google.common.base.Function;
import com.babyfs.tk.commons.base.Pair;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * 设置sql参数的Function
 */
public final class SetSqlParameterFunc implements Function<Pair<MapSqlParameterSource, Object[]>, Void> {
    private final int parameterIndex;
    private final String parameterName;

    public SetSqlParameterFunc(int parameterIndex, String parameterName) {
        this.parameterIndex = parameterIndex;
        this.parameterName = parameterName;
    }

    @Override
    public Void apply(Pair<MapSqlParameterSource, Object[]> input) {
        input.first.addValue(parameterName, input.second[parameterIndex]);
        return null;
    }
}

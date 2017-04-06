package com.babyfs.tk.dal.db.funcs;

import com.google.common.base.Function;

import javax.annotation.Nullable;
import java.util.List;

/**
 *
 */
public class GetNumberFunction implements Function<List<Object[]>, Object> {
    @Override
    public Object apply(@Nullable List<Object[]> input) {
        if (input == null || input.isEmpty()) {
            return 0;
        }
        return ((Number) input.get(0)[0]).intValue();
    }
}

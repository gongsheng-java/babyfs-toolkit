package com.babyfs.tk.dal.db.funcs;

import com.google.common.base.Function;
import com.babyfs.tk.orm.IEntity;

/**
 * 从方法的参数中提取{@link IEntity}
 */
public final class EntityParameterFunc<T extends IEntity> implements Function<Object[], T> {
    private final int parameterIndex;

    public EntityParameterFunc(int parameterIndex) {
        this.parameterIndex = parameterIndex;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T apply(Object[] input) {
        return (T) input[parameterIndex];
    }
}

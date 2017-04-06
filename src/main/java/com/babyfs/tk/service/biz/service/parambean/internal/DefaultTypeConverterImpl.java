package com.babyfs.tk.service.biz.service.parambean.internal;

import com.google.common.base.Strings;
import com.babyfs.tk.service.biz.service.parambean.ITypeConverter;

/**
 * 提供一个默认的{@link ITypeConverter}实现
 * <p/>
 */
public class DefaultTypeConverterImpl implements ITypeConverter {
    @Override
    public <T> T convert(Class<T> targetType, String value) {
        if (targetType == String.class) {
            return (T) value;
        } else if (targetType == Integer.class) {
            return (T) (Strings.isNullOrEmpty(value) ? null : Integer.valueOf(value));
        } else if (targetType == Long.class) {
            return (T) (Strings.isNullOrEmpty(value) ? null : Long.valueOf(value));
        } else if (targetType == Boolean.class) {
            return (T) (Strings.isNullOrEmpty(value) ? null : Boolean.valueOf(value));
        } else if (targetType == Double.class) {
            return (T) (Strings.isNullOrEmpty(value) ? null : Double.valueOf(value));
        } else {
            return (T) value;
        }
    }
}

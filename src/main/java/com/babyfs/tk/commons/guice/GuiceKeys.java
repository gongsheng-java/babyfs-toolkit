package com.babyfs.tk.commons.guice;

import com.google.inject.Key;
import com.google.inject.util.Types;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 */
public final class GuiceKeys {
    private GuiceKeys() {

    }

    /**
     * 构建类似<code>Map&lt;String,String&gt;</code>的无注解的Key,例如:
     * <pre>
     *     Key<Map<String,String>> key = getSimpleKey(Map.class,String.class,String.class);
     *     Key<Set<String>> key = getSimpleKey(Set.class,String.class);
     * </pre>
     *
     * @param rawType
     * @param typeArguments
     * @param <T>
     * @return
     */
    public static <T> Key<T> getSimpleKey(Type rawType, Type... typeArguments) {
        ParameterizedType parameterizedType = Types.newParameterizedType(rawType, typeArguments);
        return (Key<T>) Key.get((Type) parameterizedType);
    }

    /**
     * 构建带注解的Key对象,例如:
     * <pre>
     *     Key<Set<String>> key = getKey(Set.class,Names.named("Set"),String.class);
     * </pre>
     *
     * @param rawType       原生的类型
     * @param annotation    注解对象
     * @param typeArguments 原生类型的参数的类型
     * @param <T>           Key的类型参数
     * @return
     */
    public static <T> Key<T> getKey(Type rawType, Annotation annotation, Type... typeArguments) {
        ParameterizedType parameterizedType = Types.newParameterizedType(rawType, typeArguments);
        return (Key<T>) Key.get((Type) parameterizedType, annotation);
    }

    /**
     * 构建带注解的Key对象,例如:
     * <pre>
     *     Key<Set<String>> key = getKey(Set.class,annotationClass,String.class);
     * </pre>
     *
     * @param rawType
     * @param annotationClass
     * @param typeArguments
     * @param <T>
     * @return
     */
    public static <T> Key<T> getKey(Type rawType, Class<? extends Annotation> annotationClass, Type... typeArguments) {
        ParameterizedType parameterizedType = Types.newParameterizedType(rawType, typeArguments);
        return (Key<T>) Key.get((Type) parameterizedType, annotationClass);
    }
}

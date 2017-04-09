package com.babyfs.tk.commons.utils;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 列表数据工具类
 * <p/>
 */
public final class ListUtil {
    private static final Object[] EMPTY_ARRAY = new Object[0];

    private ListUtil() {

    }

    /**
     * 转换两个列表的数据,封装guava List.transform 实现
     * <p/>
     * <p/>
     * 要求所有代码中必须使用该方法进行处理，不可直接使用List.transform
     * <p/>
     * List.transform并不直接生成转换后的数据，而是在实际get数据时候进行转换
     * 这里将转换完成，封装到新的List中
     * <p/>
     *
     * @param fromList
     * @param function
     * @param <F>
     * @param <T>
     * @return
     */
    public static <F, T> List<T> transform(List<F> fromList, Function<? super
            F, ? extends T> function) {
        return Lists.newArrayList(Lists.transform(fromList, function));
    }

    /**
     * 遍历迭代对象，使每个迭代的元素被指定Function调用
     * <p/>
     * <p/>
     * 该方法不关心传入Function的返回值，因此Function的返回值是{@link Void}
     * <p/>
     * 对于关心返回值的需求，请使用{@link ListUtil#transform(List, com.google.common.base.Function)}
     *
     * @param iterable
     * @param function
     * @param <F>
     */
    public static <F> void iterateViaFunc(final Iterable<F> iterable,
                                          final Function<? super F, Void> function) {
        checkNotNull(iterable);
        Iterator<F> iterator = iterable.iterator();
        checkNotNull(iterator);
        checkNotNull(function);
        while (iterator.hasNext()) {
            function.apply(iterator.next());
        }
    }

    /**
     * 检查List是否为null或者为空
     *
     * @param list
     * @return
     */
    public static boolean isEmpty(List list) {
        return list == null || list.isEmpty();
    }

    /**
     * 检查List列表是否不为空
     *
     * @param list
     * @return
     */
    public static boolean isNotEmtpy(List list) {
        return list != null && !list.isEmpty();
    }


    /**
     * 将list转为数组
     *
     * @param list
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] toArray(List<T> list) {
        if (ListUtil.isEmpty(list)) {
            return (T[]) EMPTY_ARRAY;
        }
        Object[] objs = new Object[list.size()];
        list.toArray(objs);
        return (T[]) objs;
    }

    /**
     * 将集合转为list
     *
     * @param collection
     * @param <T>
     * @return
     */
    public static <T> List<T> toList(Collection<T> collection) {
        if (collection == null || collection.isEmpty()) {
            return Collections.emptyList();
        }

        List<T> lists = Lists.newArrayList();
        collection.forEach(lists::add);
        return lists;
    }
}

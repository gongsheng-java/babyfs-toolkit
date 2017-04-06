package com.babyfs.tk.service.basic.utils;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.babyfs.tk.commons.base.Pair;

import java.util.List;

/**
 * List转换工具
 */
public final class ListTransformUtils {

    protected static final PairFirstFunction FIRST_FUNCTION = new PairFirstFunction();
    protected static final PairSecondFunction SECOND_FUNCTION = new PairSecondFunction();

    private ListTransformUtils() {

    }

    /**
     * 转换DAL加载出来的Object[]结果集为ID列表结果
     *
     * @param results
     * @return
     */
    public static List<Long> transDalObjResToLong(List<Object[]> results){
        Preconditions.checkArgument(results != null, "results can't be null.");
        if (results == null || results.isEmpty()) {
            return Lists.newArrayListWithCapacity(0);
        }
        List<Long> ids = Lists.newArrayList();
        for (Object[] objects : results) {
            ids.add(Long.parseLong(objects[0].toString()));
        }
        return ids;
    }

    /**
     * 从Pair List中抽取First组成新的List
     *
     * @param list
     * @param <FIRST>
     * @param <SECOND>
     * @return
     */
    public static <FIRST, SECOND> List<FIRST> extractFirst(List<Pair<FIRST, SECOND>> list) {
        return Lists.transform(list, FIRST_FUNCTION);
    }

    /**
     * 从Pair List中抽取Second组成新的List
     *
     * @param list
     * @param <FIRST>
     * @param <SECOND>
     * @return
     */
    public static <FIRST, SECOND> List<SECOND> extractSecond(List<Pair<FIRST, SECOND>> list) {
        return Lists.transform(list, SECOND_FUNCTION);
    }

    private static class PairFirstFunction<FIRST, SECOND> implements Function<Pair<FIRST, SECOND>, FIRST> {
        @Override
        public FIRST apply(Pair<FIRST, SECOND> input) {
            return input.first;
        }
    }

    private static class PairSecondFunction<FIRST, SECOND> implements Function<Pair<FIRST, SECOND>, SECOND> {
        @Override
        public SECOND apply(Pair<FIRST, SECOND> input) {
            return input.second;
        }
    }

}

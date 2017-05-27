package com.babyfs.tk.commons.utils;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

/**
 *
 */
public class ListUtilTest {
    @Test
    public void splitList() throws Exception {
        List<List<Object>> splitList = ListUtil.splitList(Collections.emptyList(), 100);
        Assert.assertEquals(1, splitList.size());

        for (int batch = 1000; batch <= 1100; batch++) {
            List<Integer> ids = Lists.newArrayList();
            for (int i = 0; i < batch; i++) {
                ids.add(i);
            }

            for (int i = 1; i <= 2020; i++) {
                List<List<Integer>> splitIds = ListUtil.splitList(ids, i);
                List<Integer> allIds = Lists.newArrayList();
                splitIds.forEach(allIds::addAll);
                Assert.assertEquals(ids, allIds);
            }
        }
    }
}
package com.babyfs.tk.service.biz.list.impl;

import com.babyfs.tk.commons.base.Pair;
import com.babyfs.tk.commons.utils.ListUtil;
import com.babyfs.tk.dal.DalUtil;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
abstract class ListDataServiceSupport {
    static List<Pair<Long, Long>> parseListResult(boolean targetAsScoreId, List<Object[]> objects) {
        if (targetAsScoreId) {
            List<Long> list = DalUtil.extractColumn(objects, 0);
            return list.stream().map(targetId -> Pair.of(targetId, targetId)).collect(Collectors.toList());
        } else {
            if (ListUtil.isNotEmtpy(objects)) {
                List<Pair<Long, Long>> ret = Lists.newArrayListWithCapacity(objects.size());
                for (Object[] obj : objects) {
                    ret.add(Pair.of((Long) obj[0], (Long) obj[1]));
                }
                return ret;
            }
        }
        return Collections.emptyList();
    }
}


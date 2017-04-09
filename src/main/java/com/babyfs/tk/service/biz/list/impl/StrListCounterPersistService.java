package com.babyfs.tk.service.biz.list.impl;

import com.babyfs.tk.commons.base.Pair;
import com.babyfs.tk.service.biz.base.entity.list.BaseStrListCounterEntity;
import com.babyfs.tk.service.biz.counter.ICounterPersistService;
import com.babyfs.tk.service.biz.list.dal.IBaseStrListCounterDao;
import com.babyfs.tk.dal.DalUtil;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 列表服务的计数计数持久化服务
 */
public class StrListCounterPersistService<C extends BaseStrListCounterEntity> implements ICounterPersistService {
    /**
     * {@link BaseStrListCounterEntity#getCounter()}的字段短名称
     */
    public static final String FIELD_COUNTER_VALUE = "v";

    private final IBaseStrListCounterDao<C> counterDao;

    /**
     * @param counterDao not null
     */
    public StrListCounterPersistService(IBaseStrListCounterDao<C> counterDao) {
        this.counterDao = Preconditions.checkNotNull(counterDao);
    }

    /**
     * 构建列表计数器的字段及值Map
     *
     * @param delta
     * @return
     */
    public static List<Pair<String, Long>> buildCounterField(long delta) {
        List<Pair<String, Long>> fields = Lists.newArrayListWithCapacity(1);
        fields.add(Pair.of(FIELD_COUNTER_VALUE, delta));
        return fields;
    }

    /**
     * 取得列表计数器的值
     *
     * @param counterField not null
     * @return
     */
    public static long getCounterValue(Map<String, Long> counterField) {
        Long value = counterField.get(FIELD_COUNTER_VALUE);
        //防止value在异常情况下变为负数
        if (value != null && value >= 0) {
            return value;
        } else {
            return 0;
        }
    }

    @Override
    public Map<String, Long> get(int type, String id) {
        Long counter = DalUtil.extractFirst(this.counterDao.getByOwnerId(id));
        HashMap<String, Long> map = Maps.newHashMap();
        if (counter == null) {
            map.put(FIELD_COUNTER_VALUE, 0L);
        } else {
            map.put(FIELD_COUNTER_VALUE, counter);
        }
        return map;
    }

    @Override
    public boolean del(int type, String id) {
        return this.counterDao.delByOwnerId(id) > 0;
    }

    @Override
    public boolean sync(int type, String id, Map<String, Long> fieldAndValue) {
        Preconditions.checkArgument(fieldAndValue.get(FIELD_COUNTER_VALUE) != null, "no field " + FIELD_COUNTER_VALUE);
        Long value = fieldAndValue.get(FIELD_COUNTER_VALUE);
        if (value < 0) {
            value = 0L;
        }
        return this.counterDao.insertOrUpdate(id, value) > 0;
    }
}

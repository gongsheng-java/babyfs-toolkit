package com.babyfs.tk.service.biz.base;


import com.babyfs.tk.commons.enums.IndexedEnum;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * CMS用户活动类型
 */
public enum ActivityLogType implements IndexedEnum, IWithSubType {
    ACCOUNT(1, "帐户") {
        private final List<AccountSubType> all = Collections.unmodifiableList(Arrays.asList(AccountSubType.values()));

        @Override
        public List<AccountSubType> getSubTypes() {
            return all;
        }

        @Override
        public AccountSubType indexOfSubType(int index) {
            return AccountSubType.indexOf(index);
        }
    };

    private static final List<ActivityLogType> INDEXS = Util.toIndexes(ActivityLogType.values());

    private final int index;
    private final String name;

    ActivityLogType(int index, String name) {
        this.index = index;
        this.name = name;
    }


    @Override
    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    /**
     * 根据index 获取指定的枚举
     *
     * @param index
     * @return
     */
    public static ActivityLogType indexOf(final int index) {
        return Util.valueOf(INDEXS, index);
    }
}
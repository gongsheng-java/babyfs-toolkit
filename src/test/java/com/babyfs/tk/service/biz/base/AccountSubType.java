package com.babyfs.tk.service.biz.base;


import com.babyfs.tk.commons.enums.IndexedEnum;

import java.util.List;

/**
 * CMS用户帐户活动子类型
 */
public enum AccountSubType implements IndexedEnum, ISubType {
    LOGIN(1, "登录"),
    LOGOUT(2, "退出"),
    CHANGE_PASSWORD(3, "修改密码");

    private static final List<AccountSubType> INDEXS = Util.toIndexes(AccountSubType.values());

    private final int index;
    private final String name;

    AccountSubType(int index, String name) {
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
    public static AccountSubType indexOf(final int index) {
        return Util.valueOf(INDEXS, index);
    }
}
package com.babyfs.tk.service.biz.service.backend.user.model.bean;

import com.babyfs.tk.commons.enums.IndexedEnum;

import java.util.List;

/**
 * 内置的账号类型
 */
public enum AccountType implements IAccountType, IndexedEnum {
    /**
     * LDAP账号
     */
    LDAP(0, "ldap"),
    /**
     * 内部账号
     */
    INTERNAL(1, "inte");

    private static final List<AccountType> INDEXS = Util.toIndexes(AccountType.values());

    private final int type;
    private final String suffix;

    AccountType(int type, String suffix) {
        this.type = type;
        this.suffix = suffix;
    }

    @Override
    public int getIndex() {
        return type;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public String getSuffix() {
        return suffix;
    }

    public static AccountType indexOf(final int index) {
        return Util.valueOf(INDEXS, index);
    }
}


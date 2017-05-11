package com.babyfs.tk.service.biz.op.user.model;

/**
 * 账号的类型
 */
public interface IAccountType {
    /**
     * 账号的类型
     *
     * @return
     */
    int getType();

    /**
     * 账号的后缀
     *
     * @return
     */
    String getSuffix();
}
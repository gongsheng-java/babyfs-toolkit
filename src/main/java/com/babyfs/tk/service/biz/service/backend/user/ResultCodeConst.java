package com.babyfs.tk.service.biz.service.backend.user;

import com.babyfs.tk.commons.model.ServiceResponse;

/**
 * 返回结果中 code 值的定义
 */
public final class ResultCodeConst {
    /**
     * 成功
     */
    public static final int SUCCESS = ServiceResponse.SUCCESS_KEY;
    /**
     * 失败
     */
    public static final int FAULT = ServiceResponse.FAIL_KEY;
    /**
     * 服务器参数校验未通过
     */
    public static final int PARAMETER_ERROR = 100;

    /**
     * 用户名或密码错误
     */
    public static final int LOGIN_ERROR = 101;

    /**
     * 用户还未注册
     */
    public static final int NOT_REG = 102;

    /**
     * 用户已经注册了
     */
    public static final int ALREADY_REG = 103;

    /**
     * 已经存在
     */
    public static final int ALREADY_EXIST = 104;

    /**
     * 不存在
     */
    public static final int NOT_EXIST = 105;

    /**
     * 需要登录
     */
    public static final int LOGIN_REQUIRED = 106;

    /**
     * 权限不足
     */
    public static final int NO_PERMISSION = 107;
    /**
     * 账户无效
     */
    public static final int ACCOUNT_NOT_ENABLED = 108;

    private ResultCodeConst() {
    }
}

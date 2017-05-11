package com.babyfs.tk.service.biz.constants;


import com.babyfs.tk.commons.model.ServiceResponse;

/**
 * 错误代码定义
 */
public class ErrorCode {
    public static final int SUCCESS = ServiceResponse.SUCCESS_KEY;
    public static final int FAIL = ServiceResponse.FAIL_KEY;
    /**
     * 参数错误
     */
    public static final int PARAM_ERROR = 400;
    /**
     * 未认证
     */
    public static final int UNAUTHORIZED = 401;
    /**
     * 内部错误
     */
    public static final int INTERNAL_ERROR = 500;

    protected ErrorCode() {

    }
}
package com.babyfs.tk.commons.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.babyfs.tk.service.biz.constants.ErrorCode;

import java.io.Serializable;

/**
 * 服务调用返回结果
 */
public class ServiceResponse<T> implements Serializable {
    private static final long serialVersionUID = -7105469190103583078L;

    /**
     * 成功的code
     */
    public static final int SUCCESS_KEY = 0;

    /**
     * 失败的code
     */
    public static final int FAIL_KEY = 1;

    /**
     * 通用的失败响应
     */
    public static final ServiceResponse FAIL_RESPONSE = createFailResponse(FAIL_KEY, null);

    /**
     * 通用的成功响应
     */
    public static final ServiceResponse SUCCESS_RESPONSE = createSuccessResponse(null);

    /**
     * 通用的未找到数据的失败响应
     */
    public static final ServiceResponse FAIL_NOT_FOUND_RESPONSE = createFailResponse(ErrorCode.NODATA_ERROR, "未找到数据");

    /**
     * 通用的未找到数据的失败响应
     */
    public static final ServiceResponse FAIL_EXISTED_RESPONSE = createFailResponse(ErrorCode.EXISTED, "已经存在");

    /**
     * 参数错误的响应
     */
    public static final ServiceResponse PARAM_ERROR_RESPONSE = ServiceResponse.createFailResponse(ErrorCode.PARAM_ERROR, "参数错误");

    /**
     * 状态错误的响应
     */
    public static final ServiceResponse STATUS_ERROR_RESPONSE = ServiceResponse.createFailResponse(ErrorCode.PARAM_ERROR, "状态错误");

    /**
     * 调用结果成功还是失败
     */
    private final boolean success;

    /**
     * 结果代码
     */
    private final int code;

    /**
     * 数据
     */
    private final T data;

    /**
     * 结果的描述
     */
    private final String msg;

    /**
     * @param success 是否成功
     * @param code    结果代码
     * @param data    结果数据
     * @param msg     结果描述
     */
    public ServiceResponse(boolean success, int code, T data, String msg) {
        this.success = success;
        this.code = code;
        this.data = data;
        this.msg = msg;
    }

    public ServiceResponse() {
        this.success = false;
        this.code = 0;
        this.data = null;
        this.msg = null;
    }

    public T getData() {
        return data;
    }

    public boolean isSuccess() {
        return success;
    }

    @JSONField(serialize = false)
    public boolean isFailure() {
        return !success;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    /**
     * 是否未找到数据
     *
     * @return
     */
    public boolean isNotFound() {
        return this.code == ErrorCode.NODATA_ERROR;
    }

    /**
     * 构建成功的响应
     *
     * @param data
     * @return
     */
    public static <T> ServiceResponse<T> createSuccessResponse(T data) {
        return new ServiceResponse<T>(true, SUCCESS_KEY, data, null);
    }

    /**
     * 构建成功的响应
     *
     * @param data
     * @param msg
     * @return
     */
    public static <T> ServiceResponse<T> createSuccessResponse(T data, String msg) {
        return new ServiceResponse<T>(true, SUCCESS_KEY, data, msg);
    }

    /**
     * 构建错误的响应
     *
     * @param code 错误代码
     * @param msg  错误描述
     * @return
     */
    public static <T> ServiceResponse<T> createFailResponse(int code, String msg) {
        return new ServiceResponse<T>(false, code, null, msg);
    }

    /**
     * 构建错误的响应
     *
     * @param msg 错误描述
     * @return
     */
    public static <T> ServiceResponse<T> createFailResponse(String msg) {
        return new ServiceResponse<T>(false, ServiceResponse.FAIL_KEY, null, msg);
    }

    /**
     * 构建错误的响应
     *
     * @param code
     * @param data
     * @param msg
     * @param <T>
     * @return
     */
    public static <T> ServiceResponse<T> createFailResponse(int code, T data, String msg) {
        return new ServiceResponse<T>(false, code, data, msg);
    }

    /**
     * 返回默认的失败响应
     *
     * @param <T>
     * @return
     * @see {@link #FAIL_RESPONSE}
     */
    @SuppressWarnings("unchecked")
    public static <T> ServiceResponse<T> defaultFailResponse() {
        return (ServiceResponse<T>) FAIL_RESPONSE;
    }

    /**
     * 空的失败响应
     *
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> ServiceResponse<T> failResponse() {
        return FAIL_RESPONSE;
    }

    /**
     * 空的成功响应
     *
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> ServiceResponse<T> succResponse() {
        return SUCCESS_RESPONSE;
    }

    /**
     * 空的未找到数据响应
     *
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> ServiceResponse<T> notFoundResponse() {
        return FAIL_NOT_FOUND_RESPONSE;
    }

    /**
     * 参数错误
     *
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> ServiceResponse<T> paramErrorResponse() {
        return PARAM_ERROR_RESPONSE;
    }

    /**
     * 状态错误
     *
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> ServiceResponse<T> statusErrorResponse() {
        return STATUS_ERROR_RESPONSE;
    }

    /**
     * 已经存在
     *
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> ServiceResponse<T> existedErrorResponse() {
        return FAIL_EXISTED_RESPONSE;
    }
}

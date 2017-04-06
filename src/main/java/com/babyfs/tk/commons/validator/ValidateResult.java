package com.babyfs.tk.commons.validator;

/**
 * 数据验证的返回结果
 * <p/>
 */
public class ValidateResult {
    public static final ValidateResult RESULT_OK = new ValidateResult(true, "");
    /**
     * 验证结果，true表示验证通过
     */
    private final boolean success;

    /**
     * 验证失败的错误消息
     */
    private final String errorMsg;

    /**
     * @param success  验证结果，true表示验证通过
     * @param errorMsg 验证失败的详细信息
     */
    public ValidateResult(boolean success, String errorMsg) {
        this.success = success;
        this.errorMsg = errorMsg;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMsg() {
        return errorMsg;
    }
}

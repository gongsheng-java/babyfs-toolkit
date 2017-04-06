package com.babyfs.tk.commons.validator.rule;

/**
 * 验证规则接口
 * <p/>
 */
public interface IValidateRule {
    /**
     * 验证value的值是否合法
     *
     * @param value 需要验证的值
     * @return true表示验证成功，false表示失败
     */
    public boolean validate(String value);

    /**
     * 返回验证错误信息
     *
     * @return
     */
    public String getErrorMsg();
}

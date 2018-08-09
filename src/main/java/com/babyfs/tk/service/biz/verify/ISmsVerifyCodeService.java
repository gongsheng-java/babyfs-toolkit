package com.babyfs.tk.service.biz.verify;


/**
 * 短信验证码服务接口
 */
public interface ISmsVerifyCodeService {
    /**
     * 创建验证码,需要以下参数
     * <ul>
     * <li>{@link SmsCodeParameter#mobile} not null</li>
     * <li>{@link SmsCodeParameter#type} </li>
     * <li>{@link SmsCodeParameter#second} > 0</li>
     * </ul>
     *
     * @param parameter
     * @return
     */
    String createCode(SmsCodeParameter parameter);

    /**
     * 校验验证码,需要以下参数
     * <ul>
     * <li>{@link SmsCodeParameter#mobile} not null</li>
     * <li>{@link SmsCodeParameter#type} not null</li>
     * <li>{@link SmsCodeParameter#toCheckCode} not null</li>
     * </ul>
     *
     * @param parameter
     * @return
     */
    boolean checkCode(SmsCodeParameter parameter);

    /**
     * 检验验证码是否通过验证了,需要以下参数
     * <ul>
     * <li>{@link SmsCodeParameter#mobile} not null</li>
     * <li>{@link SmsCodeParameter#type} not null</li>
     * </ul>
     *
     * @param parameter
     * @return
     */
    boolean checkCodeVerified(SmsCodeParameter parameter);

    /**
     * 删除验证码,需要以下参数
     * <ul>
     * <li>{@link SmsCodeParameter#mobile} not null</li>
     * <li>{@link SmsCodeParameter#type} not null</li>
     * </ul>
     *
     * @param parameter
     */
    void delete(SmsCodeParameter parameter);

    void delete(String mobile, int type);
}

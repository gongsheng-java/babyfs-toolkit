package com.babyfs.tk.service.biz.verify;

import com.google.common.base.Preconditions;

/**
 * 短信验证码参数
 */
public class SmsCodeParameter {
    /**
     * 验证码类型
     */
    private int type;
    /**
     * 手机号
     */
    private String mobile;
    /**
     * 验证码失效时间
     */
    private int second;
    /**
     * 被验证的验证码
     */
    private String toCheckCode;
    /**
     * toCheckCode验证成功后是否记录验证成功的状态
     */
    private boolean recordCheckPass;
    /**
     * toCheckCode验证通过状态的失效时间
     */
    private int recordCheckPassSecond;

    public SmsCodeParameter() {

    }

    /**
     * @param mobile 手机号
     * @param type   短信类型
     */
    public SmsCodeParameter(String mobile, int type) {
        this.type = type;
        this.mobile = mobile;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }

    public boolean isRecordCheckPass() {
        return recordCheckPass;
    }

    public void setRecordCheckPass(boolean recordCheckPass) {
        this.recordCheckPass = recordCheckPass;
    }

    public int getRecordCheckPassSecond() {
        return recordCheckPassSecond;
    }

    public void setRecordCheckPassSecond(int recordCheckPassSecond) {
        this.recordCheckPassSecond = recordCheckPassSecond;
    }

    public String getToCheckCode() {
        return toCheckCode;
    }

    public void setToCheckCode(String toCheckCode) {
        this.toCheckCode = toCheckCode;
    }

    /**
     * 创建用于创建验证码的参数
     *
     * @param type
     * @param mobile
     * @param seoncd
     * @return
     */
    public SmsCodeParameter buildCreateCodeParamter(int type, String mobile, int seoncd) {
        SmsCodeParameter codeParameter = new SmsCodeParameter();
        codeParameter.setType(type);
        codeParameter.setMobile(Preconditions.checkNotNull(mobile));
        codeParameter.setSecond(seoncd);
        return codeParameter;
    }

    /**
     * 创建用于验证验证码的参数
     *
     * @param type
     * @param mobile
     * @param toCheckCode
     * @return
     */
    public SmsCodeParameter buildCheckCodeParamter(int type, String mobile, String toCheckCode) {
        SmsCodeParameter codeParameter = new SmsCodeParameter();
        codeParameter.setType(type);
        codeParameter.setMobile(Preconditions.checkNotNull(mobile));
        codeParameter.setToCheckCode(toCheckCode);
        return codeParameter;
    }
}

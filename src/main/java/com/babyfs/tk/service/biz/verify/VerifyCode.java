package com.babyfs.tk.service.biz.verify;

/**
 * 验证码
 */
public class VerifyCode {
    /**
     * 验证码值
     */
    private int code;
    /**
     * 是否已经通过验证
     */
    private boolean verified;

    public VerifyCode() {

    }

    public VerifyCode(int code, boolean verified) {
        this.code = code;
        this.verified = verified;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

}

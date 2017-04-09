package com.babyfs.tk.service.biz.freq;

import com.google.common.base.Preconditions;

/**
 * 频率频次校检参数
 */
public class FreqParameter {
    /**
     * 类型
     */
    private int type;

    /**
     * 频次，如果为1为校验频率
     */
    private int timeLimit;

    /**
     * 频率，有效时间
     */
    private int expireSecond;

    /**
     * 每次更新频次是否需要延长有效时间
     */
    private boolean isUpdateExpire;

    public FreqParameter(int type, int timeLimit, int expireSecond, boolean isUpdateExpire) {
        Preconditions.checkArgument(timeLimit >= 0, "time is invalid ");
        this.type = type;
        this.timeLimit = timeLimit;
        this.expireSecond = expireSecond;
        this.isUpdateExpire = isUpdateExpire;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public int getExpireSecond() {
        return expireSecond;
    }

    public void setExpireSecond(int expireSecond) {
        this.expireSecond = expireSecond;
    }

    public boolean isUpdateExpire() {
        return isUpdateExpire;
    }

    public void setUpdateExpire(boolean isChangeExpire) {
        this.isUpdateExpire = isChangeExpire;
    }

}

package com.babyfs.tk.service.biz.web.backend;

/**
 * @ClassName LogSwitch
 * @Author zhanghongyun
 * @Date 2018/8/27 上午11:38
 **/
public class LogSwitch {

    public boolean isLogSwitch() {
        return logSwitch;
    }

    public void setLogSwitch(boolean logSwitch) {
        this.logSwitch = logSwitch;
    }

    private boolean logSwitch;
}

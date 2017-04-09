package com.babyfs.tk.service.biz.base.model;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * 消息基础类
 */
public class Message {
    private int type;

    @JSONField(name = "type")
    public int getType() {
        return type;
    }

    @JSONField(name = "type")
    public void setType(int type) {
        this.type = type;
    }
}

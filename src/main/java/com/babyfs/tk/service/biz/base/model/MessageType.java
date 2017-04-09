package com.babyfs.tk.service.biz.base.model;


import com.babyfs.tk.commons.enums.IndexedEnum;

/**
 * 消息的类型
 */
public interface MessageType extends IndexedEnum {
    /**
     * 未知
     */
    MessageType UNKOWN = new Index(0);

    /**
     * 本地缓存变化
     */
    MessageType LOCAL_CACHE_CHANGE = new Index(1);

    class Index extends IndexedEnum.Index implements MessageType{
        public Index(int index) {
            super(index);
        }
    }
};

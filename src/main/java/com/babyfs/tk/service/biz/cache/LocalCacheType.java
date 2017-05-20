package com.babyfs.tk.service.biz.cache;

import com.babyfs.tk.commons.enums.IndexedEnum;

/**
 * 本地缓存的类型
 */
public interface LocalCacheType extends IndexedEnum {
    /**
     * 未知
     */
    LocalCacheType UNKOWN = new Index(0);
    /**
     * KV按名称缓存
     */
    LocalCacheType KV_CONF_NAME = new Index(1);
    /**
     * KV按ID缓存
     */
    LocalCacheType KV_CONF_ID = new Index(2);

    class Index extends IndexedEnum.Index implements LocalCacheType {
        public Index(int index) {
            super(index);
        }
    }
}

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
     * KV配置
     */
    LocalCacheType KV_CONF = new Index(1);

    class Index extends IndexedEnum.Index implements LocalCacheType{
        public Index(int index) {
            super(index);
        }
    }
}

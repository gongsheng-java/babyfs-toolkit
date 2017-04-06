package com.babyfs.tk.http.upload.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * 目录Cache,避免频繁地检查目录是否存在
 */
public class DirCacheLoader extends CacheLoader<String, Boolean> {

    /**
     * 创建一个目录的缓存
     *
     * @return
     */
    public static LoadingCache<String, Boolean> createDirCache() {
        return CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES).
                maximumSize(1000).
                build(new DirCacheLoader());
    }

    @Override
    public Boolean load(String key) throws Exception {
        File dir = new File(key);
        if (dir.isDirectory()) {
            return true;
        }
        if (!dir.mkdirs()) {
            throw new RuntimeException("Can't create the parend dir " + key);
        }
        return true;
    }
}

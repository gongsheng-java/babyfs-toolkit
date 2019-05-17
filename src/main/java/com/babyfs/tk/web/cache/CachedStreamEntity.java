package com.babyfs.tk.web.cache;

/**
 * @author gaowei
 * @date 2018/12/20
 */
public interface CachedStreamEntity {

    CachedStream getCachedStream();

    void flushStream();

}
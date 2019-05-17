package com.babyfs.tk.web.cache;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;

/**
 * @author gaowei
 * @date 2018/12/20
 */
public class CachedHttpServletRequestWrapper extends HttpServletRequestWrapper implements CachedStreamEntity {

    private CachedInputStream cachedInputStream;

    public CachedHttpServletRequestWrapper(HttpServletRequest httpServletRequest, int initCacheSize, int maxCacheSize)
            throws IOException {
        super(httpServletRequest);
        this.cachedInputStream = new CachedInputStream(httpServletRequest, initCacheSize, maxCacheSize);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return cachedInputStream;
    }

    @Override
    public CachedStream getCachedStream() {
        return cachedInputStream;
    }

    @Override
    public void flushStream() {
        //do nothing
    }
}
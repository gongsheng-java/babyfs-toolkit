package com.babyfs.tk.web.cache;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 可以缓存从流中读取的数据的代理流类，用于日志记录
 * @author gaowei
 * @date 2018/12/20
 */
public class CachedInputStream extends ServletInputStream implements CachedStream{
    private ByteArrayOutputStream cachedOutputStream;
    private HttpServletRequest request;
    private int maxCacheSize;

    public CachedInputStream(HttpServletRequest request, int initCacheSize, int maxCacheSize) {
        CachedStreamUtils.checkCacheSizeParam(initCacheSize, maxCacheSize);
        this.request = request;
        this.cachedOutputStream = new ByteArrayOutputStream(initCacheSize);
        this.maxCacheSize = maxCacheSize;
    }

    @Override
    public int read() throws IOException {
        int b = request.getInputStream().read();
        if(b != -1 && cachedOutputStream.size() < maxCacheSize) {
            CachedStreamUtils.safeWrite(cachedOutputStream, b);
        }
        return b;
    }

    @Override
    public byte[] getCached(){
        return cachedOutputStream.toByteArray();
    }

    @Override
    public void close() throws IOException {
        super.close();
        cachedOutputStream.close();
    }

}
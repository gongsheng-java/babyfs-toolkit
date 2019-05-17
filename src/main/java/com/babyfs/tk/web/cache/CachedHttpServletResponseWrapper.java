package com.babyfs.tk.web.cache;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * @author gaowei
 * @date 2018/12/20
 */
public class CachedHttpServletResponseWrapper extends HttpServletResponseWrapper implements CachedStreamEntity {
    private CachedOutputStream cachedOutputStream;
    private PrintWriter printWriter;

    public CachedHttpServletResponseWrapper(HttpServletResponse cachedOutputStream, int initCacheSize, int maxCacheSize)
            throws IOException {
        super(cachedOutputStream);
        this.cachedOutputStream = new CachedOutputStream(cachedOutputStream.getOutputStream(), initCacheSize,
                maxCacheSize);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return cachedOutputStream;
    }

    @Override
    public CachedStream getCachedStream() {
        return cachedOutputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if(printWriter == null){
            printWriter = new PrintWriter(new OutputStreamWriter(cachedOutputStream, getCharacterEncoding()));
        }
        return printWriter;
    }

    @Override
    public void flushStream() {
        if(printWriter != null){
            printWriter.flush();
        }
    }
}
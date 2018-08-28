package com.babyfs.tk.service.biz.web.backend;

import org.apache.commons.io.IOUtils;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @ClassName RequestWrapper
 * @Author zhanghongyun
 * @Date 2018/8/21 下午3:08
 **/
public class RequestWrapper extends HttpServletRequestWrapper {

    private byte[] data;

    /**
     * Constructs a request object wrapping the given request.
     *
     * @param request
     * @throws IllegalArgumentException if the request is null
     */
    public RequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        data = IOUtils.toByteArray(request.getInputStream());
    }

    public ServletInputStream getInputStream(){
        return new MyServletInputStream(new ByteArrayInputStream(data));
    }

    public byte[] toByteArray(){
        return data;
    }

    class MyServletInputStream extends ServletInputStream{
        private InputStream inputStream;

        public MyServletInputStream(InputStream inputStream){
            this.inputStream = inputStream;
        }

        @Override
        public int read() throws IOException {
            return inputStream.read();
        }
    }
}

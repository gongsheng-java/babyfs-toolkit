package com.babyfs.tk.service.biz.web.backend;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.*;

/**
 * Class ResponseWrapper
 * Author zhanghongyun
 * Date 2018-08-21
 */
public class ResponseWrapper extends HttpServletResponseWrapper {

    private ByteArrayOutputStream bytes;
    private ServletOutputStream out;
    private PrintWriter pwrite;

    public ResponseWrapper(HttpServletResponse response) throws IOException {
        super(response);
        bytes = new ByteArrayOutputStream();// 真正存储数据的流
        out = new MyServletOutputStream(bytes);
        pwrite = new PrintWriter(new OutputStreamWriter(bytes, "UTF-8"));
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return out; // 将数据写到 byte 中
    }

    /**
     * 重写父类的 getWriter() 方法，将响应数据缓存在 PrintWriter 中
     */
    @Override
    public PrintWriter getWriter() throws UnsupportedEncodingException {
        return pwrite;
    }

    /** 重载父类获取flushBuffer的方法 */
    @Override
    public void flushBuffer() throws IOException {
        if (out != null) {
            out.flush();
        }
        if (pwrite != null) {
            pwrite.flush();
        }
    }

    @Override
    public void reset() {
        bytes.reset();
    }

    /** 将out、writer中的数据强制输出到WapperedResponse的buffer里面，否则取不到数据 */
    public byte[] getBytes() throws IOException {
        flushBuffer();
        byte[] retBytes = bytes.toByteArray();
        return retBytes;
    }

    class MyServletOutputStream extends ServletOutputStream {
        private ByteArrayOutputStream ostream ;

        public MyServletOutputStream(ByteArrayOutputStream ostream) {
            this.ostream = ostream;
        }

        @Override
        public void write(int b) throws IOException {
            ostream.write(b); // 将数据写到 stream　中
        }

        @Override
        public void write(byte[] b) throws IOException {
            ostream.write(b, 0, b.length);
        }

    }

}
package com.babyfs.tk.rpc.util;

import com.google.common.base.Preconditions;
import com.babyfs.tk.commons.Constants;
import org.jboss.netty.buffer.ChannelBuffer;

import java.io.UnsupportedEncodingException;

/**
 */
public final class IOUtil {
    private IOUtil() {
    }

    /**
     * 写一个String到buffer中
     *
     * @param str
     * @param buffer
     * @throws RuntimeException
     */
    public static void writeString(String str, ChannelBuffer buffer) {
        if (str == null || str.isEmpty()) {
            buffer.writeInt(0);
            return;
        }
        try {
            byte[] data = str.getBytes(Constants.UTF_8);
            buffer.writeInt(data.length);
            buffer.writeBytes(data);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从当前位置读一个String
     *
     * @param buffer
     * @throws RuntimeException
     */
    public static String readString(ChannelBuffer buffer) {
        int length = buffer.readInt();
        Preconditions.checkState(length >= 0, "The string length is %d", length);
        if (length == 0) {
            return null;
        }
        byte[] data = new byte[length];
        buffer.readBytes(data);
        try {
            return new String(data, Constants.UTF_8);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}

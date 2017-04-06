package com.babyfs.tk.commons.utils;

import com.babyfs.tk.commons.Constants;
import com.google.common.base.Function;

import javax.annotation.Nullable;
import java.io.UnsupportedEncodingException;

/**
 * 常用的Funciton实现
 */
public class FunctionUtil {
    private FunctionUtil() {

    }

    /**
     * String -> byte[] 的转换函数
     */
    public static class StringToByteArray implements Function<String, byte[]> {
        @Override
        public byte[] apply(@Nullable String input) {
            if (input == null) {
                return null;
            }
            try {
                return input.getBytes(Constants.DEFAULT_CHARSET);
            } catch (UnsupportedEncodingException e) {
                //ignore it
            }
            return null;
        }
    }

    /**
     * byte[] -> String 的转换函数
     */
    public static class ByteArrayToString implements Function<byte[], String> {
        @Override
        public String apply(@Nullable byte[] input) {
            if (input == null) {
                return null;
            }
            try {
                return new String(input, Constants.DEFAULT_CHARSET);
            } catch (UnsupportedEncodingException e) {
                //ignore it
            }
            return null;
        }
    }
}

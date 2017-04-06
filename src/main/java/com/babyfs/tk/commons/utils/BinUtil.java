package com.babyfs.tk.commons.utils;

/**
 * Binary数据处理工具
 * <p/>
 */
public final class BinUtil {

    /**
     * 默认的long型转byte的数组长度
     */
    public static final int LONGBYTESIZE = 8;

    private BinUtil() {

    }

    /**
     * 将byte数组转为long : 8位byte数组
     *
     * @param data
     * @return
     */
    public static long byte2long(byte[] data) {

        if (data == null || data.length < LONGBYTESIZE) {
            return -1;
        }
        return getLong(data, 0);

    }

    /**
     * 将long转为8位byte数组
     *
     * @param data
     * @return
     */
    public static byte[] long2byte(long data) {

        byte[] bytes = new byte[LONGBYTESIZE];
        putLong(bytes, data, 0);
        return bytes;

    }


    /**
     * long处理工具，默认8个字节
     * <p/>
     * 将long放入byte数组中指定索引的位置
     *
     * @param bytes
     * @param x
     * @param index
     */
    public static void putLong(byte[] bytes, long x, int index) {
        bytes[index + 0] = (byte) (x >> 56);
        bytes[index + 1] = (byte) (x >> 48);
        bytes[index + 2] = (byte) (x >> 40);
        bytes[index + 3] = (byte) (x >> 32);
        bytes[index + 4] = (byte) (x >> 24);
        bytes[index + 5] = (byte) (x >> 16);
        bytes[index + 6] = (byte) (x >> 8);
        bytes[index + 7] = (byte) (x);
    }

    /**
     * long处理工具，默认8个字节
     * <p/>
     * 从byte数组中获取指定索引的long
     *
     * @param bytes
     * @param index
     */
    public static long getLong(byte[] bytes, int index) {
        return ((((long) bytes[index] & 0xff) << 56)
                | (((long) bytes[index + 1] & 0xff) << 48)
                | (((long) bytes[index + 2] & 0xff) << 40)
                | (((long) bytes[index + 3] & 0xff) << 32)
                | (((long) bytes[index + 4] & 0xff) << 24)
                | (((long) bytes[index + 5] & 0xff) << 16)
                | (((long) bytes[index + 6] & 0xff) << 8) | (((long) bytes[index + 7] & 0xff)));
    }

}

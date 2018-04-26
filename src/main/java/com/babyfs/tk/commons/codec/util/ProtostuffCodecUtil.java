package com.babyfs.tk.commons.codec.util;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

/**
 * 基于Protostuff的加码解码工具类
 */
public class ProtostuffCodecUtil {

    public ProtostuffCodecUtil() {
    }

    /**
     * 对象转换成数组
     *
     * @param obj
     * @param <T>
     * @return
     */
    public static <T> byte[] encode(T obj) {
        Class<T> clazz = (Class<T>) obj.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        Schema<T> schema = RuntimeSchema.getSchema(clazz);
        final byte[] result;
        try {
            result = ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } finally {
            buffer.clear();
        }
        return result;
    }

    /**
     * 数组转换成对象
     *
     * @param data
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T decode(byte[] data, Class<T> clazz) {
        Schema<T> schema = RuntimeSchema.getSchema(clazz);
        T object = schema.newMessage();
        ProtostuffIOUtil.mergeFrom(data, object, schema);
        return object;
    }
}

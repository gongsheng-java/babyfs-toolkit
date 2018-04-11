/**
 * 使用Hessian实现的编解码器
 */

package com.babyfs.tk.commons.codec.util;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.SerializerFactory;
import com.babyfs.tk.commons.codec.CodecException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public final class HessianCodecUtil {
    private static final SerializerFactory DEFAULT_SER_FACTORY = new SerializerFactory();
    private static final int SIZE = 512;

    private HessianCodecUtil() {

    }

    /**
     * 对象变成byte数组
     *
     * @param obj
     * @return
     */
    public static byte[] encode(Object obj) {
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream(SIZE);
        final Hessian2Output output = new Hessian2Output();
        output.init(byteArray);
        output.setSerializerFactory(DEFAULT_SER_FACTORY);
        try {
            output.writeObject(obj);
            output.flush();
            return byteArray.toByteArray();
        } catch (IOException e) {
            throw new CodecException(e);
        } finally {
            try {
                output.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    /**
     * 将byte数组转回对象
     *
     * @param data
     * @return
     */
    public static Object decode(byte[] data) {
        final Hessian2Input input = new Hessian2Input();
        input.init(new ByteArrayInputStream(data));
        input.setSerializerFactory(DEFAULT_SER_FACTORY);
        try {
            return input.readObject();
        } catch (IOException e) {
            throw new CodecException(e);
        } finally {
            try {
                input.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }
}

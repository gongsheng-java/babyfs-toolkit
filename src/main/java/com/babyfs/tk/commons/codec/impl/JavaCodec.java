package com.babyfs.tk.commons.codec.impl;

import com.babyfs.tk.commons.codec.CodecException;
import com.babyfs.tk.commons.codec.CodecTypes;
import com.babyfs.tk.commons.codec.ICodec;
import com.google.common.io.Closeables;

import java.io.*;

/**
 * 使用Java内置的序列化和反序列化实现的编解码器
 */
public class JavaCodec implements ICodec {

    public JavaCodec() {
    }

    public byte getType() {
        return CodecTypes.JAVA_CODEC;
    }

    /**
     * @param obj
     * @return
     */
    public byte[] encode(Object obj) {
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        ObjectOutputStream output = null;
        try {
            output = new ObjectOutputStream(byteArray);
            output.writeObject(obj);
            output.flush();
            return byteArray.toByteArray();
        } catch (IOException e) {
            throw new CodecException(e);
        } finally {
            try {
                Closeables.close(output, true);
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    /**
     * @param data
     * @return
     */
    public Object decode(byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }
        ObjectInputStream objectIn = null;
        try {
            objectIn = new ObjectInputStream(new ByteArrayInputStream(data));
            return objectIn.readObject();
        } catch (Exception e) {
            throw new CodecException(e);
        } finally {
            try {
                Closeables.close(objectIn, true);
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    public Object decode(byte[] data, Object instanceCreator) {
        throw new UnsupportedOperationException();
    }
}

package com.babyfs.tk.service.basic.redis.test;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 */
public class HessianCodec {
    private static final Logger LOGGER = LoggerFactory.getLogger(HessianCodec.class);

    public byte[] encode(Object obj) {
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        Hessian2Output output = new Hessian2Output(byteArray);
        try {
            output.writeObject(obj);
            output.flush();
            output.close();
            return byteArray.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                output.close();
            } catch (IOException e) {
                LOGGER.error("Close output error.", e);
            }
        }
    }

    public Object decode(byte[] data) {
        final Hessian2Input input = new Hessian2Input(new ByteArrayInputStream(data));
        try {
            return input.readObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                input.close();
            } catch (IOException e) {
                LOGGER.error("Close output error.", e);
            }
        }
    }

    public Object decode(byte[] data, Object instanceCreator) {
        throw new UnsupportedOperationException();
    }
}


package com.babyfs.tk.galaxy.codec;


import com.babyfs.tk.commons.codec.util.HessianCodecUtil;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * 解码接口
 */
public interface Decoder {


    Object decode(byte[] response, Type type) throws IOException;

    /**
     * 默认的hession解码器
     */
    public class Default implements Decoder {

        @Override
        public Object decode(byte[] response, Type type) throws IOException {
            return HessianCodecUtil.decode(response);
        }
    }
}

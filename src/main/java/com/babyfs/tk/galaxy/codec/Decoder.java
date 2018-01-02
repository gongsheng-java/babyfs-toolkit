
package com.babyfs.tk.galaxy.codec;


import com.babyfs.tk.commons.codec.util.HessianCodecUtil;

import java.io.IOException;

/**
 * 解码器接口
 */
public interface Decoder {

    Object decode(byte[] response) throws IOException;

    /**
     * 默认的Hessian解码器
     */
    class Default implements Decoder {
        @Override
        public Object decode(byte[] response) throws IOException {
            return HessianCodecUtil.decode(response);
        }
    }
}

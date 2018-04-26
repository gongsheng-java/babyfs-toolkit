
package com.babyfs.tk.galaxy.codec;


import com.babyfs.tk.commons.codec.util.ProtostuffCodecUtil;

/**
 * 解码器接口
 */
public interface IDecoder {

    <T> T decode(byte[] response, Class<T> result);

    /**
     * 默认的Protostuff解码器
     */
    class Default implements IDecoder {

        @Override
        public <T> T decode(byte[] response, Class<T> result) {
            return ProtostuffCodecUtil.decode(response, result);
        }
    }
}

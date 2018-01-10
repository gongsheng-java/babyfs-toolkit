
package com.babyfs.tk.galaxy.codec;


import com.babyfs.tk.commons.codec.util.HessianCodecUtil;

/**
 * 编码器接口
 */
public interface IEncoder {

    byte[] encode(Object object);

    /**
     * 默认的Hessian编码器
     */
    class Default implements IEncoder {
        @Override
        public byte[] encode(Object object) {
            return HessianCodecUtil.encode(object);
        }
    }
}


package com.babyfs.tk.galaxy.codec;


import com.babyfs.tk.commons.codec.util.HessianCodecUtil;

import java.lang.reflect.Type;

/**
 * 编码器接口
 */
public interface Encoder {

    byte[] encode(Object object);

    /**
     * 默认的Hessian编码器
     */
    class Default implements Encoder {
        @Override
        public byte[] encode(Object object) {
            return HessianCodecUtil.encode(object);
        }
    }
}

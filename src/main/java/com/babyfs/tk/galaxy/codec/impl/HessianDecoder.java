package com.babyfs.tk.galaxy.codec.impl;

import com.babyfs.tk.commons.codec.util.HessianCodecUtil;
import com.babyfs.tk.galaxy.codec.IDecoder;

/**
 * 默认的Hessian解码器
 */
public class HessianDecoder implements IDecoder {
    @Override
    public Object decode(byte[] response) {
        return HessianCodecUtil.decode(response);
    }
}

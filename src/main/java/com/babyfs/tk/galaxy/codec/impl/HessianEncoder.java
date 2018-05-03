package com.babyfs.tk.galaxy.codec.impl;

import com.babyfs.tk.commons.codec.util.HessianCodecUtil;
import com.babyfs.tk.galaxy.codec.IEncoder;

/**
 * 默认的Hessian编码器
 */
public class HessianEncoder implements IEncoder {
    @Override
    public byte[] encode(Object object) {
        return HessianCodecUtil.encode(object);
    }
}

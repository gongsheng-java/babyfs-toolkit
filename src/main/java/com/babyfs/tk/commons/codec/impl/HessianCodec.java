package com.babyfs.tk.commons.codec.impl;

import com.babyfs.tk.commons.codec.ICodec;
import com.babyfs.tk.commons.codec.util.HessianCodecUtil;
import com.babyfs.tk.commons.codec.CodecTypes;

/**
 * 使用Hessian实现的编解码器
 */
public class HessianCodec implements ICodec {

    public HessianCodec() {
    }

    public byte getType() {
        return CodecTypes.HESSIAN_CODEC;
    }

    public byte[] encode(Object obj) {
        return HessianCodecUtil.encode(obj);
    }

    public Object decode(byte[] data) {
        return HessianCodecUtil.decode(data);
    }

    public Object decode(byte[] data, Object instanceCreator) {
        throw new UnsupportedOperationException();
    }
}

package com.babyfs.tk.commons.codec.impl;

import com.babyfs.tk.commons.codec.CodecTypes;
import com.babyfs.tk.commons.codec.ICodec;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;

/**
 * Google protobuff的编码器
 */
public class ProtobuffCodec implements ICodec {

    public ProtobuffCodec() {
    }

    public byte getType() {
        return CodecTypes.PROTOBUFF_CODEC;
    }

    public byte[] encode(Object obj) {
        if (obj == null) {
            return new byte[0];
        }
        if (!(obj instanceof Message)) {
            throw new IllegalArgumentException(
                    "The obj must be a com.google.protobuf.Message" + obj);
        }
        return ((Message) obj).toByteArray();
    }

    public Object decode(byte[] data) {
        throw new UnsupportedOperationException("Please use decode(Message,byte[]) instead");
    }

    public Object decode(byte[] data, Object instanceCreator) {
        if (data == null || data.length == 0) {
            return null;
        }
        if (!(instanceCreator instanceof Message)) {
            throw new IllegalArgumentException("Except a " + Message.class + " object");
        }
        try {
            return ((Message) instanceCreator).newBuilderForType().mergeFrom(data).build();
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("Can't decode for type " + instanceCreator.getClass(), e);
        }
    }
}

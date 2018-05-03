package com.babyfs.tk.commons.name.impl.zookeeper;

import com.babyfs.tk.commons.codec.ICodec;
import com.babyfs.tk.commons.name.model.gen.NamingServices;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Zookeeper节点二进制数据的编码/解码器,使用google protocols buffer编码格式
 */
public class ServerNodeProtoCodec implements ICodec {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerNodeProtoCodec.class);

    @Override
    public byte getType() {
        return 1;
    }

    @Override
    public byte[] encode(Object obj) {
        return ((Message) obj).toByteArray();
    }

    @Override
    public Object decode(byte[] data) {
        try {
            return NamingServices.NSServer.getDefaultInstance().newBuilderForType().mergeFrom(data).build();
        } catch (InvalidProtocolBufferException e) {
            LOGGER.error("Decode fail.", e);
            return null;
        }
    }

    @Override
    public Object decode(byte[] data, Object instanceCreator) {
        throw new UnsupportedOperationException();
    }
}

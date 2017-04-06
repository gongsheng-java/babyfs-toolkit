package com.babyfs.tk.rpc.protocol.impl;

import com.google.common.base.Preconditions;
import com.babyfs.tk.commons.codec.ICodec;
import com.babyfs.tk.rpc.Request;
import com.babyfs.tk.rpc.codec.Codecs;
import com.babyfs.tk.rpc.util.IOUtil;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 * 对{@link Request} 对象的二进制协议的封装
 */
public class BinaryRequestProtocol extends BinaryBaseProtocol<Request> {
    /**
     * 消息的版本号
     */
    public static final byte MSG_VERSION = 1;
    /**
     * 默认的Buffer初始长度
     */
    public static final int DEFAULT_BUFFER_INIT_LENGTH = 256;

    public BinaryRequestProtocol() {
        super(BinaryProtocol.RPC_TYPE, BinaryProtocol.RPC_VERSION, BinaryProtocol.MSG_TYPE_REQUEST, MSG_VERSION);
    }


    public ChannelBuffer encode(Request request) {
        ChannelBuffer buffer = ChannelBuffers.dynamicBuffer(DEFAULT_BUFFER_INIT_LENGTH);
        this.encode(request, buffer);
        return buffer;
    }

    protected void encodeBody(Request request, ChannelBuffer channelBuffer) {
        final ICodec codec = Codecs.getCodecByType(request.getCodecType());
        Preconditions.checkNotNull(codec, "Can't find tye codec for codec type %s.", request.getCodecType());
        //write body
        {
            channelBuffer.writeInt(request.getId());
            channelBuffer.writeByte(request.getCodecType());
            IOUtil.writeString(request.getServiceName(), channelBuffer);
            IOUtil.writeString(request.getMethodName(), channelBuffer);
            IOUtil.writeString(request.getMethodId(), channelBuffer);
            final Object[] parameters = request.getParameters();
            if (parameters == null) {
                channelBuffer.writeShort(0);
            } else {
                channelBuffer.writeShort(parameters.length);
                for (int i = 0; i < parameters.length; i++) {
                    byte[] data = codec.encode(parameters[i]);
                    channelBuffer.writeInt(data.length);
                    channelBuffer.writeBytes(data);
                }
            }
        }
    }

    protected Request decodeBody(ChannelBuffer channelBuffer) {
        final int id = channelBuffer.readInt();
        final byte codecType = channelBuffer.readByte();
        final ICodec codec = Codecs.getCodecByType(codecType);
        Preconditions.checkNotNull(codec, "Can't find the codec for codec type %s.", codecType);
        String serviceName = IOUtil.readString(channelBuffer);
        String methodName = IOUtil.readString(channelBuffer);
        String methodId = IOUtil.readString(channelBuffer);
        short parameterCount = channelBuffer.readShort();
        Object[] paramters = new Object[parameterCount];
        Request request = new Request(serviceName, methodName, methodId, id, paramters);
        request.setCodecType(codecType);
        if (parameterCount > 0) {
            if (Codecs.needInstanceCreaotr(codecType)) {
                //ProtoBuffer类型的编码在这时是未知的,需要到运行时决定
                request.setParamterParsed(false);
                request.setData(channelBuffer);
            } else {
                for (int i = 0; i < parameterCount; i++) {
                    int dataLength = channelBuffer.readInt();
                    byte[] data = new byte[dataLength];
                    channelBuffer.readBytes(data);
                    paramters[i] = codec.decode(data);
                }
                request.setParamterParsed(true);
            }
        } else {
            request.setParamterParsed(true);
        }
        return request;
    }


}

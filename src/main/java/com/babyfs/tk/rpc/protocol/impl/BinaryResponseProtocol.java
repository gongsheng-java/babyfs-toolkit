package com.babyfs.tk.rpc.protocol.impl;

import com.google.common.base.Preconditions;
import com.babyfs.tk.commons.codec.ICodec;
import com.babyfs.tk.rpc.Response;
import com.babyfs.tk.rpc.codec.Codecs;
import com.babyfs.tk.rpc.util.IOUtil;
import org.jboss.netty.buffer.ChannelBuffer;

/**
 * 对{@link Response}对象的二进制协议封装
 */
public final class BinaryResponseProtocol extends BinaryBaseProtocol<Response> {
    private static final byte MSG_VERSION = 1;

    public BinaryResponseProtocol() {
        super(BinaryProtocol.RPC_TYPE, BinaryProtocol.RPC_VERSION, BinaryProtocol.MSG_TYPE_RESPONSE, MSG_VERSION);
    }

    @Override
    protected void encodeBody(Response response, ChannelBuffer channelBuffer) {
        final ICodec codec = Codecs.getCodecByType(response.getCodecType());
        Preconditions.checkNotNull(codec, "Can't find tye codec for codec type %s.", response.getCodecType());
        //write body
        {
            channelBuffer.writeInt(response.getId());
            channelBuffer.writeByte(response.getCodecType());
            channelBuffer.writeByte(response.isSuccess() ? 1 : 0);
            IOUtil.writeString(response.getErrormsg(), channelBuffer);
            Object responseObj = response.getResponse();
            byte[] data = codec.encode(responseObj);
            channelBuffer.writeInt(data.length);
            channelBuffer.writeBytes(data);
        }
    }

    @Override
    protected Response decodeBody(ChannelBuffer channelBuffer) {
        //read body
        Response response = new Response();
        final int id = channelBuffer.readInt();
        final byte codecType = channelBuffer.readByte();
        final ICodec codec = Codecs.getCodecByType(codecType);
        Preconditions.checkNotNull(codec, "Can't find the codec for codec type %s.", codecType);
        boolean success = channelBuffer.readByte() == 1;
        String errorMsg = IOUtil.readString(channelBuffer);
        response.setId(id);
        response.setSuccess(success);
        response.setCodecType(codecType);
        response.setErrormsg(errorMsg);
        channelBuffer.markReaderIndex();
        int length = channelBuffer.readInt();
        if (length > 0) {
            channelBuffer.resetReaderIndex();
            if (Codecs.needInstanceCreaotr(codecType)) {
                //ProtoBuffer类型的编码在这时是未知的,需要到运行时决定
                response.setParamterParsed(false);
                response.setData(channelBuffer);
            } else {
                byte[] data = new byte[channelBuffer.readInt()];
                channelBuffer.readBytes(data);
                Object responseObj = codec.decode(data);
                response.setResponse(responseObj);
                response.setParamterParsed(true);
            }
        } else {
            response.setParamterParsed(true);
        }
        return response;
    }
}

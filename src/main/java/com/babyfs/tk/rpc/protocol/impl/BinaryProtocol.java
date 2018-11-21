package com.babyfs.tk.rpc.protocol.impl;

import com.google.common.base.Preconditions;
import com.babyfs.tk.rpc.RPC;
import com.babyfs.tk.rpc.Request;
import com.babyfs.tk.rpc.Response;
import com.babyfs.tk.rpc.protocol.IProtocol;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 * RPC二进制协议定义,协议格式的整体结构为:HEADER+BODY,其中Header部分是固定的,BODY部分相对可变
 * <pre>
 *
 * HEADER的结构:
 * TYPE,协议类型,byte,1B
 * MSG_VERSION,协议的版本号,byte,1B
 * MSG_TYPE,消息的类型,short,2B
 * MSG_VERSION,消息的版本号(即MSG_TYPE类型消息的版本号),byte,1B
 *
 * BODY的结构:
 * 1. Request body
 * REQUEST_ID,请求的ID号,int,4B
 * CODEC_TYPE,消息的编码类型,byte,1B
 * SERVICE_NAME_LENGTH,服务名称的长度,short,2B
 * SERVICE_NAME,服务名称,String bytes,SERVICE_NAME_LENGTH
 * METHOD_NAME_LENGTH,方法名称的长度,short,2B
 * METHOD_NAME,方法名称,String bytes,METHOD_NAME_LENGTH
 * METHOD_ID_LENGTH,方法名称的ID,short,2B
 * METHOD_ID,方法名称的ID,String bytes,METHOD_ID
 * PARAMETER_COUNT,参数的个数,short,2B
 * PARAMETERS,参数的数据,PARAMETER,sum(PARAMETERS[PARAMETER_COUNT])
 *
 * PARAMETER:
 * PARAMETER_LENGTH,参数的长度,int,4B
 * PARAMETER_DATA,参数的数据,byte[],PARAMETER_LENGTH
 *
 * 2. Response body
 * REQUEST_ID,请求的ID号,int,4B
 * CODEC_TYPE,消息的编码类型,byte,1B
 * SUCCESS,是否成功,byte,1B (0 失败;1 成功)
 * ERROR_MSG_LENGTH,如果哦SUCCESS为false,则该项数据有意义
 * ERROR_MSG,错误消息
 * DATA_LENGTH,响应的数据的长度,int,4B
 * DATA,响应的数据,byte[],DATA_LENGTH
 *
 * </pre>
 */
public final class BinaryProtocol implements IProtocol<RPC, ChannelBuffer> {
    /**
     * RPC协议的类型
     */
    public static final byte RPC_TYPE = 1;
    /**
     * RPC协议的版本号
     */
    public static final byte RPC_VERSION = 1;
    /**
     * RPC消息类型:Request
     */
    public static final short MSG_TYPE_REQUEST = 1;
    /**
     * RPC消息类型:Response
     */
    public static final short MSG_TYPE_RESPONSE = 2;

    private final BinaryRequestProtocol requestProtocol = new BinaryRequestProtocol();
    private final BinaryResponseProtocol responseProtocol = new BinaryResponseProtocol();

    /**
     * @param obj
     * @return
     */
    public ChannelBuffer encode(RPC obj) {
        ChannelBuffer channelBuffer = ChannelBuffers.dynamicBuffer(256);
        encode(obj, channelBuffer);
        return channelBuffer;
    }

    public void encode(RPC o, ChannelBuffer channelBuffer) {
        Preconditions.checkNotNull(o);
        Preconditions.checkNotNull(channelBuffer);
        if (o instanceof Request) {
            requestProtocol.encode((Request) o, channelBuffer);
        } else if (o instanceof Response) {
            responseProtocol.encode((Response) o, channelBuffer);
        } else {
            throw new UnsupportedOperationException("Only support " + Request.class + "," + Response.class);
        }
    }

    public RPC decode(ChannelBuffer channelBuffer) {
        Preconditions.checkNotNull(channelBuffer);
        channelBuffer.markReaderIndex();
        byte protocolType = channelBuffer.readByte();
        byte protocolVersion = channelBuffer.readByte();
        short msgType = channelBuffer.readShort();
        byte msgVersion = channelBuffer.readByte();
        channelBuffer.resetReaderIndex();
        //check protocl version
        Preconditions.checkState(protocolType == RPC_TYPE, "The protocl type is %d,but %d is excepted.", protocolType, RPC_TYPE);
        Preconditions.checkState(protocolVersion <= RPC_VERSION, "The version %d > supported version %d", protocolVersion, RPC_VERSION);
//        Preconditions.checkState(msgVersion>=0,"Bad msgVersion:%s",msgVersion);


        switch (msgType) {
            case MSG_TYPE_REQUEST:
                return requestProtocol.decode(channelBuffer);
            case MSG_TYPE_RESPONSE:
                return responseProtocol.decode(channelBuffer);
            default:
                throw new IllegalStateException("Unknown msg type:" + msgType);
        }
    }
}

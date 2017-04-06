package com.babyfs.tk.rpc.protocol.impl;

import com.babyfs.tk.rpc.RPC;
import com.babyfs.tk.rpc.protocol.IProtocol;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 * 二进制协议的基础类
 */
public abstract class BinaryBaseProtocol<E extends RPC> implements IProtocol<E, ChannelBuffer> {
    protected final byte protocolType;
    protected final byte protocolVersion;
    protected final short msgType;
    protected final byte msgVersion;

    protected BinaryBaseProtocol(byte protocolType, byte protocolVersion, short msgType, byte msgVersion) {
        this.protocolType = protocolType;
        this.protocolVersion = protocolVersion;
        this.msgType = msgType;
        this.msgVersion = msgVersion;
    }

    public ChannelBuffer encode(E request) {
        ChannelBuffer buffer = ChannelBuffers.dynamicBuffer(256);
        this.encode(request, buffer);
        return buffer;
    }


    public void encode(E rpc, ChannelBuffer channelBuffer) {
        //write header
        channelBuffer.writeByte(this.protocolType);
        channelBuffer.writeByte(this.protocolVersion);
        channelBuffer.writeShort(this.msgType);
        channelBuffer.writeByte(this.msgVersion);
        //set the protocol info to the request object.
        rpc.setProtocolType(this.protocolType);
        rpc.setProtocolVersion(this.protocolVersion);
        rpc.setMsgType(this.msgType);
        rpc.setMsgVersion(this.msgVersion);
        //write body
        this.encodeBody(rpc, channelBuffer);
    }

    public E decode(ChannelBuffer channelBuffer) {
        //read header
        byte tProtocolType = channelBuffer.readByte();
        byte tProtocolVersion = channelBuffer.readByte();
        short tMsgType = channelBuffer.readShort();
        byte version = channelBuffer.readByte();
        //read body
        {
            E rpc = decodeBody(channelBuffer);
            rpc.setProtocolType(tProtocolType);
            rpc.setProtocolVersion(tProtocolVersion);
            rpc.setMsgType(tMsgType);
            rpc.setMsgVersion(version);
            return rpc;
        }
    }

    /**
     * 消息体编码
     *
     * @param rpc
     * @param channelBuffer
     */
    protected abstract void encodeBody(E rpc, ChannelBuffer channelBuffer);

    /**
     * 解码消息体
     *
     * @param channelBuffer
     * @return
     */
    protected abstract E decodeBody(ChannelBuffer channelBuffer);
}

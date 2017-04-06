package com.babyfs.tk.rpc;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * RPC请求基类
 */
public class RPC {
    /**
     * rpc调用的id
     */
    protected int id;
    /**
     * 协议类型
     */
    protected byte protocolType;
    /**
     * 协议的版本号
     */
    protected byte protocolVersion;
    /**
     * 协议的消息类型
     */
    protected short msgType;
    /**
     * 协议的消息版本号
     */
    protected short msgVersion;
    /**
     * 编码类型
     */
    protected byte codecType;

    /**
     * 参数是否已经解析,是为了适应google protocolbuffer这种编码信息中没有数据类型信息的情况
     * 延迟服务调用时在决定参数的类型
     */
    protected boolean paramterParsed = true;
    /**
     * 从网络上接受的数据
     */
    protected ChannelBuffer data;


    public RPC() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public byte getProtocolType() {
        return protocolType;
    }

    public void setProtocolType(byte protocolType) {
        this.protocolType = protocolType;
    }

    public byte getCodecType() {
        return codecType;
    }

    public byte getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(byte protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public short getMsgType() {
        return msgType;
    }

    public void setMsgType(short msgType) {
        this.msgType = msgType;
    }

    public short getMsgVersion() {
        return msgVersion;
    }

    public void setMsgVersion(short msgVersion) {
        this.msgVersion = msgVersion;
    }

    public void setCodecType(byte codecType) {
        this.codecType = codecType;
    }

    public ChannelBuffer getData() {
        return data;
    }

    public void setData(ChannelBuffer data) {
        this.data = data;
    }

    public boolean isParamterParsed() {
        return paramterParsed;
    }

    public void setParamterParsed(boolean paramterParsed) {
        this.paramterParsed = paramterParsed;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RPC)) return false;

        RPC rpc = (RPC) o;

        if (codecType != rpc.codecType) return false;
        if (id != rpc.id) return false;
        if (msgType != rpc.msgType) return false;
        if (msgVersion != rpc.msgVersion) return false;
        if (protocolType != rpc.protocolType) return false;
        if (protocolVersion != rpc.protocolVersion) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (int) protocolType;
        result = 31 * result + (int) protocolVersion;
        result = 31 * result + (int) msgType;
        result = 31 * result + (int) msgVersion;
        result = 31 * result + (int) codecType;
        return result;
    }
}

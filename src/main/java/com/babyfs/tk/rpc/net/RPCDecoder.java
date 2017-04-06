package com.babyfs.tk.rpc.net;

import com.babyfs.tk.rpc.RPC;
import com.babyfs.tk.rpc.protocol.IProtocol;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;

/**
 * RPC协议解码器
 */
public class RPCDecoder extends OneToOneDecoder {
    private final IProtocol<RPC, ChannelBuffer> protocol;

    public RPCDecoder(IProtocol<RPC, ChannelBuffer> protocol) {
        this.protocol = protocol;
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
        if (!(msg instanceof ChannelBuffer)) {
            return msg;
        }
        return protocol.decode((ChannelBuffer) msg);
    }
}

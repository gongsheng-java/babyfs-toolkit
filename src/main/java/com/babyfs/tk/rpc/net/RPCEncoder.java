package com.babyfs.tk.rpc.net;

import com.babyfs.tk.rpc.RPC;
import com.babyfs.tk.rpc.protocol.IProtocol;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

/**
 * RPC协议编码器
 */
public class RPCEncoder extends OneToOneEncoder {
    private final IProtocol<RPC, ChannelBuffer> protocol;

    public RPCEncoder(IProtocol<RPC, ChannelBuffer> protocol) {
        this.protocol = protocol;
    }

    @Override
    protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
        if (!(msg instanceof RPC)) {
            return msg;
        }
        return this.protocol.encode((RPC) msg);
    }
}

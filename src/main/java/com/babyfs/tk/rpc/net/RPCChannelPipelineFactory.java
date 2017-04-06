package com.babyfs.tk.rpc.net;

import com.babyfs.tk.rpc.protocol.Protocols;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.LengthFieldPrepender;


/**
 * Netty管道工厂
 */
public final class RPCChannelPipelineFactory {
    private RPCChannelPipelineFactory() {

    }

    /**
     * 创建二进制的RPC协议ChannelPipelineFactory
     *
     * @param bizHandler 业务处理器
     * @return
     */
    public static ChannelPipelineFactory makeBinaryPipelineFactory(final ChannelUpstreamHandler bizHandler) {
        return new BinaryChannelPipelineFactory(bizHandler);
    }

    private static class BinaryChannelPipelineFactory implements ChannelPipelineFactory {
        private final ChannelUpstreamHandler bizHandler;

        public BinaryChannelPipelineFactory(ChannelUpstreamHandler bizHandler) {
            this.bizHandler = bizHandler;
        }

        public ChannelPipeline getPipeline() throws Exception {
            ChannelPipeline pipeline = Channels.pipeline();
            // 启用frameDecoder解析带长度属性的数据包 ，当接收到指定长度的数据后才会交给handler处理, 数据包最大长度为1024 ＊512 ， 长度属性为4字节
            pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(1024 * 512, 0, 4, 0, 4));
            pipeline.addLast("rcpDecoder", new RPCDecoder(Protocols.BINARY_PROTOCOL));

            pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
            pipeline.addLast("rpcEncoder", new RPCEncoder(Protocols.BINARY_PROTOCOL));
            if (bizHandler != null) {
                pipeline.addLast("bizHandler", bizHandler);
            }
            return pipeline;
        }
    }
}

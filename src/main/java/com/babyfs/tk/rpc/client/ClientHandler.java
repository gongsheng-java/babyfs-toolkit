package com.babyfs.tk.rpc.client;

import com.google.common.base.Preconditions;
import com.babyfs.tk.rpc.Response;
import com.babyfs.tk.rpc.service.IRPCListener;
import org.jboss.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 客户端的Netty Io事件处理
 */
public class ClientHandler extends SimpleChannelUpstreamHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientHandler.class);

    /**
     * rpc事件监听
     */
    private final IRPCListener listener;


    public ClientHandler(IRPCListener listener) {
        Preconditions.checkArgument(listener != null, "The listener must be set.");
        this.listener = listener;
    }

    @Override
    public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
        Object message = e.getMessage();
        if (!(message instanceof Response)) {
            throw new Exception("Only support Response message");
        }
        final Response response = (Response) message;
        this.listener.onResponseReceived(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        Channel channel = ctx.getChannel();
        channel.close();
        LOGGER.error("Catch a fatal exception,close the channel [" + channel.toString() + "]", e.getCause());
    }
}

package com.babyfs.tk.rpc.server;

import com.google.common.base.Preconditions;
import com.babyfs.tk.commons.codec.ICodec;
import com.babyfs.tk.rpc.Request;
import com.babyfs.tk.rpc.Response;
import com.babyfs.tk.rpc.codec.Codecs;
import com.babyfs.tk.rpc.service.ServerServiceProxy;
import com.babyfs.tk.rpc.service.ServiceWrapper;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

/**
 * 服务器的Io事件处理器
 */
public class ServerHandler extends SimpleChannelUpstreamHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerHandler.class);
    /**
     * 业务处理的线程池
     */
    private ExecutorService executorService;
    /**
     * 服务端的代理接口
     */
    private ServerServiceProxy serviceProxy;

    public ServerHandler(ExecutorService executorService, ServerServiceProxy serviceProxy) {
        Preconditions.checkArgument(executorService != null, "The executorService muset not be null.");
        Preconditions.checkArgument(serviceProxy != null, "The serviceProxy must not be null.");
        this.executorService = executorService;
        this.serviceProxy = serviceProxy;
    }

    @Override
    public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
        Object message = e.getMessage();
        if (!(message instanceof Request)) {
            throw new Exception("Only support Request message");
        }
        final Request request = (Request) message;
        final Response response = new Response();
        response.setId(request.getId());
        response.setCodecType(request.getCodecType());

        try {
            ICodec codec = Codecs.getCodecByType(request.getCodecType());
            //处理参数未解析的情况
            if (!request.isParamterParsed()) {
                ServiceWrapper serviceWrapper = serviceProxy.get(request.getServiceName());
                ServiceWrapper.MethodWrapper serviceMethod = serviceWrapper.getServiceMethod(request.getMethodName(), request.getMethodId());
                Object[] parameterInstanceCreator = serviceMethod.getParameterInstanceCreator();
                ChannelBuffer buffer = request.getData();
                //参数未解析
                Object[] parameters = request.getParameters();
                for (int i = 0; i < parameters.length; i++) {
                    int dataLength = buffer.readInt();
                    if (dataLength > 0) {
                        byte[] data = new byte[dataLength];
                        buffer.readBytes(data);
                        parameters[i] = codec.decode(data, parameterInstanceCreator[i]);
                    }
                }
                request.setParamterParsed(true);
                request.setData(null);
            }

            executorService.execute(new Runnable() {
                public void run() {
                    try {
                        Object o = serviceProxy.callService(request.getServiceName(), request.getMethodName(), request.getMethodId(), request.getParameters());
                        response.setResponse(o);
                        response.setSuccess(true);
                    } catch (Exception e) {
                        response.setErrormsg(e.getMessage());
                        response.setSuccess(false);
                        LOGGER.error("Process " + request.getServiceName() + "." + request.getMethodName() + " faile", e);
                    }
                    ChannelFuture writeFucure = ctx.getChannel().write(response);

                    writeFucure.addListener(new WriteChannelFutureListener(response.getId()));
                }
            });
        } catch (Exception ee) {
            response.setSuccess(false);
            response.setErrormsg("Error:" + ee.getMessage());
            ChannelFuture future = ctx.getChannel().write(response);
            future.addListener(new WriteChannelFutureListener(request.getId()));
            LOGGER.error("Process request error,id " + request.getId(), ee);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        Channel channel = ctx.getChannel();
        channel.close();
        LOGGER.error("Catch a fatal exception,close the channel [" + channel.toString() + "]", e.getCause());
    }


    /**
     * 监听写失败的情况
     */
    private static final class WriteChannelFutureListener implements ChannelFutureListener {
        private final int id;

        public WriteChannelFutureListener(int id) {
            this.id = id;
        }

        public void operationComplete(ChannelFuture future) throws Exception {

            if (future.isSuccess()) {
                return;
            }
            final Channel channel = future.getChannel();
            if (future.isCancelled()) {
                LOGGER.warn("Send Response {} is canceled,channel [{}]", id, channel);
            }
            if (future.getCause() != null) {
                //发生异常,关闭连接
                if (channel.isConnected()) {
                    channel.close();
                }
                LOGGER.error("Send response {} failed,channel [{}]", id, channel);
            }
        }
    }
}

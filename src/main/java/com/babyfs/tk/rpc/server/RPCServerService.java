package com.babyfs.tk.rpc.server;

import com.google.common.base.Preconditions;
import com.babyfs.tk.commons.service.LifeServiceSupport;
import com.babyfs.tk.commons.thread.NamedThreadFactory;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.logging.Slf4JLoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * RPC服务
 */
public class RPCServerService extends LifeServiceSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(RPCServerService.class);

    private ServerBootstrap bootstrap = null;
    private ChannelPipelineFactory pipelineFactory;

    /**
     * 监听的地址
     */
    private final String address;
    /**
     * 监听的端口
     */
    private final int port;
    private final ChannelGroup group = new DefaultChannelGroup();
    private final ChannelGroupHandler groupHandler = new ChannelGroupHandler(group);


    public RPCServerService(final String address, int port) {
        this.address = address;
        this.port = port;
    }

    protected synchronized void execStart() {
        Preconditions.checkState(bootstrap == null, "The server has already started.");
        Preconditions.checkNotNull(this.pipelineFactory, "The pipelineFactory must be set.");
        LOGGER.info("RPCServer service starting");
        // 使用Slf4jLoggerFactory
        InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());
        ThreadFactory serverBossTF = new NamedThreadFactory("RPC-SERVER-BOSS-");
        ThreadFactory serverWorkerTF = new NamedThreadFactory("RPC-SERVER-WORKER-");
        ExecutorService bossExecutor = Executors.newCachedThreadPool(serverBossTF);
        ExecutorService workerExecutor = Executors.newCachedThreadPool(serverWorkerTF);
        bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(bossExecutor, workerExecutor));
        bootstrap.setOption("tcpNoDelay", true);
        bootstrap.setOption("reuseAddress", true);
        bootstrap.setPipelineFactory(new GroupChannelPipelineFactory());
        InetSocketAddress socketAddress;
        try {
            socketAddress = new InetSocketAddress(InetAddress.getByName(this.address), this.port);
        } catch (UnknownHostException e) {
            throw new RuntimeException("Illegal address:" + this.address, e);
        }
        try {
            Channel channel = bootstrap.bind(socketAddress);
            group.add(channel);
        } catch (Exception e) {
            LOGGER.error("RPCServer service bind failed", e);
            throw new RuntimeException(e);
        }
        LOGGER.info("RPCServer service started,listen at: " + socketAddress.toString());
    }

    protected synchronized void execStop() {
        LOGGER.info("RPCServer service stoping");
        group.close().awaitUninterruptibly(5000);
        bootstrap.releaseExternalResources();
        bootstrap = null;
        LOGGER.info("RPCServer service stopped");
    }

    public String getAddress() {
        return address;
    }


    public int getPort() {
        return port;
    }

    public ChannelPipelineFactory getPipelineFactory() {
        return pipelineFactory;
    }

    public void setPipelineFactory(ChannelPipelineFactory pipelineFactory) {
        this.pipelineFactory = pipelineFactory;
    }

    private static final class ChannelGroupHandler extends SimpleChannelUpstreamHandler {
        private final ChannelGroup group;

        private ChannelGroupHandler(ChannelGroup group) {
            this.group = group;
        }

        @Override
        public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
            Channel channel = e.getChannel();
            group.add(channel);
            super.channelOpen(ctx, e);
        }
    }

    private final class GroupChannelPipelineFactory implements ChannelPipelineFactory {
        @Override
        public ChannelPipeline getPipeline() throws Exception {
            ChannelPipeline pipeline = pipelineFactory.getPipeline();
            pipeline.addFirst("channelGroup", groupHandler);
            return pipeline;
        }
    }
}

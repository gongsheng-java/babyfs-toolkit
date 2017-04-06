package com.babyfs.tk.rpc.client;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.cache.*;
import com.google.common.collect.ComputationException;
import com.google.common.collect.MapMaker;
import com.babyfs.tk.commons.service.LifeServiceSupport;
import com.babyfs.tk.commons.thread.NamedThreadFactory;
import com.babyfs.tk.rpc.Request;
import com.babyfs.tk.rpc.Response;
import com.babyfs.tk.rpc.service.IRPCListener;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.logging.Slf4JLoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;


/**
 * 基于Netty实现的RPC客户端,内置了一个简单的连接池.
 */
public class RPCClient extends LifeServiceSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(RPCClient.class);
    /**
     * 默认的连接超时时间,单位毫秒
     */
    public static final int CONNECTION_TIMEOUT = 1000;
    /**
     * 默认的在连接池中为每个服务器建立的连接数
     */
    public static final int POOL_CONNECTIONS = 3;
    /**
     * 默认的连接过期时间
     */
    public static final int POOL_EXPIRE_TIME = 60;

    private volatile ClientBootstrap bootstrap;
    private ChannelPipelineFactory channelPipelineFactory;

    /**
     * 连接建立的超时时间
     */
    private final long connectTimeout;
    /**
     * 向每个目标主机建立的个数,默认为3个
     */
    private final int connectionCountPerServer;
    /**
     * 连接失效的时间,单位秒
     */
    private final int poolExpireTime;
    /**
     * 连接池
     * key: server_address:port 服务器地址
     * value: key:the id,value:channel 随机选择的channel
     */
    private final ConcurrentMap<String, LoadingCache<Integer, Channel>> channelPool = new MapMaker().makeMap();
    /**
     * RPC事件监听集合
     */
    private final ConcurrentMap<IRPCListener, IRPCListener> listeners = new MapMaker().makeMap();
    /**
     * 是否启动
     */
    private volatile boolean start = false;

    /**
     * 使用默认的参数配置对象
     */
    public RPCClient() {
        this(CONNECTION_TIMEOUT, POOL_CONNECTIONS, POOL_EXPIRE_TIME);
    }

    /**
     * @param connectTimeout           连接建立的超时时间,单位毫秒
     * @param connectionCountPerServer 为每个服务器建立的连接数
     */
    public RPCClient(long connectTimeout, int connectionCountPerServer, int poolExpireTime) {
        Preconditions.checkArgument(connectTimeout > 0, "The connectionTimeout must be > 0.");
        Preconditions.checkArgument(connectionCountPerServer > 0, "The connectionCountPerServer must be >0.");
        this.connectTimeout = connectTimeout;
        this.connectionCountPerServer = connectionCountPerServer;
        this.poolExpireTime = poolExpireTime;
    }

    /**
     * 向RPC服务器发送一个请求,如果在请求发送的过程中发生错误,则会将通过{@link IRPCListener}接口通知所有的监听者
     *
     * @param address 服务的地址
     * @param port    服务的端口
     * @param request 请求对象
     * @throws RuntimeException
     * @see {@link #addErrorListener(IRPCListener)}
     */
    public void sendRequest(String address, int port, Request request) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(address), "The address must not be empty.");
        Preconditions.checkArgument(port > 0, "The port must be > 0.");
        Preconditions.checkArgument(request != null, "The request must not be null");
        Preconditions.checkState(start, "Please call start() to init the rpc client");

        String key = address + ":" + port;
        LoadingCache<Integer, Channel> currentChannelConcurrentMap = channelPool.get(key);
        if (currentChannelConcurrentMap == null) {
            currentChannelConcurrentMap = createPoolChannelCache(address, port);
            LoadingCache<Integer, Channel> pre = channelPool.putIfAbsent(key, currentChannelConcurrentMap);
            if (pre != null) {
                currentChannelConcurrentMap = pre;
            }
        }
        int index = new Random().nextInt(connectionCountPerServer);
        final Channel channel = currentChannelConcurrentMap.getUnchecked(index);
        if (channel == null) {
            throw new RuntimeException("Failed to connect to the target server [" + key + "]");
        }
        ChannelFuture future = channel.write(request);
        future.addListener(new WriteChannelFutureListener(key, request.getId()));
    }


    /**
     * 启动客户端
     */
    @Override
    public synchronized void execStart() {
        Preconditions.checkNotNull(channelPipelineFactory, "The channelPipelineFactory must be set.");
        // 使用Slf4jLoggerFactory
        InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());
        ThreadFactory bossThreadFactory = new NamedThreadFactory("RPC-CLIENT-BOSS-");
        ThreadFactory workerThreadFactory = new NamedThreadFactory("RPC-CLIENT-WORKER-");
        bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(Executors.newCachedThreadPool(bossThreadFactory), Executors.newCachedThreadPool(workerThreadFactory)));
        bootstrap.setOption("tcpNoDelay", true);
        bootstrap.setOption("reuseAddress", true);
        bootstrap.setOption("connectTimeoutMillis", connectTimeout);
        bootstrap.setPipelineFactory(channelPipelineFactory);
        start = true;
    }

    /**
     * 停止客户端
     */
    @Override
    public synchronized void execStop() {
        start = false;
        //close all channel
        {
            Collection<LoadingCache<Integer, Channel>> values = channelPool.values();
            for (LoadingCache<Integer, Channel> pool : values) {
                ConcurrentMap<Integer, Channel> integerChannelConcurrentMap = pool.asMap();
                for (Channel channel : integerChannelConcurrentMap.values()) {
                    channel.close();
                }

                pool.invalidateAll();
            }
        }
        channelPool.clear();
        this.bootstrap.releaseExternalResources();
    }

    /**
     * 增加一个RPC事件的监听者,当发生错误的时候通知监听者
     *
     * @param listener
     */
    public void addErrorListener(IRPCListener listener) {
        Preconditions.checkArgument(listener != null);
        this.listeners.putIfAbsent(listener, listener);
    }

    /**
     * 删除指定的监听者
     *
     * @param listener
     */
    public IRPCListener removeErrorListener(IRPCListener listener) {
        Preconditions.checkArgument(listener != null);
        return this.listeners.remove(listener);
    }

    public long getConnectTimeout() {
        return connectTimeout;
    }


    public ChannelPipelineFactory getChannelPipelineFactory() {
        return channelPipelineFactory;
    }

    public void setChannelPipelineFactory(ChannelPipelineFactory channelPipelineFactory) {
        this.channelPipelineFactory = channelPipelineFactory;
    }

    public int getConnectionCountPerServer() {
        return connectionCountPerServer;
    }

    private static class ChannelRemovalListener implements RemovalListener<Integer, Channel> {
        @Override
        public void onRemoval(RemovalNotification<Integer, Channel> notification) {
            Channel value = notification.getValue();
            if (value.isOpen()) {
                LOGGER.info("Close rpc client channel {}", value);
                value.close();
            }
        }
    }

    /**
     * 监听写失败的情况
     */
    private final class WriteChannelFutureListener implements ChannelFutureListener {
        private final String key;
        private final int id;

        public WriteChannelFutureListener(String key, int id) {
            this.key = key;
            this.id = id;
        }

        public void operationComplete(ChannelFuture future) throws Exception {
            if (future.isSuccess()) {
                return;
            }
            final Channel channel = future.getChannel();
            String errorMsg = "";
            if (future.isCancelled()) {
                errorMsg = "Request is canceled channel [" + channel + "]";
            }
            if (future.getCause() != null) {
                //发生异常,关闭连接
                if (channel.isConnected()) {
                    channel.close();
                    removeChannel(this.key, channel, false);
                }
                errorMsg = "Send request to " + channel + " error" + future.getCause();
                LOGGER.error(errorMsg, future.getCause());
            }
            //构造失败的响应
            Response response = new Response(this.id, null, false);
            response.setErrormsg(errorMsg);
            for (IRPCListener listener : listeners.values()) {
                listener.onResponseReceived(response);
            }
        }
    }

    /**
     * 为指定的地址,端口创建一个连接池
     *
     * @param address 服务器的地址
     * @param port    服务监听的端口
     * @return
     */
    private LoadingCache<Integer, Channel> createPoolChannelCache(final String address, final int port) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(address), "The address must not be null or emptyl.");
        Preconditions.checkArgument(port > 0, "The port must be > 0.");
        return CacheBuilder.newBuilder().expireAfterAccess(this.poolExpireTime, TimeUnit.SECONDS).removalListener(new ChannelRemovalListener()).build(new CacheLoader<Integer, Channel>() {
            public Channel load(Integer input) throws Exception {
                try {
                    return createChannel(address, port);
                } catch (Exception e) {
                    LOGGER.error("Create channel fail", e);
                    throw new ComputationException(e);
                }
            }
        });
    }

    /**
     * 删除一个channel
     *
     * @param key
     * @param channel
     * @param autoClose 是否自动关闭连接
     */
    private void removeChannel(String key, Channel channel, boolean autoClose) {
        Cache<Integer, Channel> cache = channelPool.get(key);
        if (cache != null) {
            Set<Map.Entry<Integer, Channel>> entries = cache.asMap().entrySet();
            Iterator<Map.Entry<Integer, Channel>> iterator = entries.iterator();
            while (iterator.hasNext()) {
                Map.Entry<Integer, Channel> next = iterator.next();
                if (next.getValue() == channel) {
                    iterator.remove();
                    cache.invalidate(next.getKey());
                }
            }
        }
        if (autoClose && channel.isConnected()) {
            channel.close();
        }
    }

    /**
     * @param targetIP
     * @param targetPort
     * @return
     * @throws Exception
     */
    private Channel createChannel(final String targetIP, final int targetPort) throws Exception {
        LOGGER.info("Create connection to " + targetIP + ":" + targetPort);
        ChannelFuture future = bootstrap.connect(new InetSocketAddress(targetIP, targetPort));
        future.awaitUninterruptibly(connectTimeout);
        if (!future.isDone()) {
            LOGGER.error("Create connection to " + targetIP + ":" + targetPort + " timeout!");
            throw new Exception("Create connection to " + targetIP + ":" + targetPort + " timeout!");
        }
        if (future.isCancelled()) {
            LOGGER.error("Create connection to " + targetIP + ":" + targetPort + " cancelled by user!");
            throw new Exception("Create connection to " + targetIP + ":" + targetPort + " cancelled by user!");
        }
        if (!future.isSuccess()) {
            LOGGER.error("Create connection to " + targetIP + ":" + targetPort + " error", future.getCause());
            throw new Exception("Create connection to " + targetIP + ":" + targetPort + " error", future.getCause());
        }
        final Channel channel = future.getChannel();
        ChannelFuture closeFuture = channel.getCloseFuture();
        closeFuture.addListener(new CloseChannelFutureListener(targetIP, targetPort));
        return channel;
    }

    private class CloseChannelFutureListener implements ChannelFutureListener {
        private final String key;
        private final String targetIP;
        private final int targetPort;

        public CloseChannelFutureListener(String targetIP, int targetPort) {
            this.targetIP = targetIP;
            this.targetPort = targetPort;
            key = targetIP + ":" + targetPort;
        }

        public void operationComplete(ChannelFuture future) throws Exception {
            removeChannel(key, future.getChannel(), false);
        }
    }
}

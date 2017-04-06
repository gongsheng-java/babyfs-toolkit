package com.babyfs.tk.rpc;

/**
 * RPC的配置项
 */
public final class RPCConfig {
    /**
     * RPC客户端配置:编码类型
     */
    public static final String CONF_RPC_CLIENT_CODEC_TYPE = "rpc.client.codec.type";
    /**
     * RPC客户端配置:请求的超时时间,单位:ms
     */
    public static final String CONF_RPC_CLIENT_TIMEOUT = "rpc.client.timeout";
    /**
     * RPC Server绑定的ip
     */
    public static final String CONF_RPC_SERVER_BINDIP = "rpc.server.bindip";
    /**
     * RPC Server绑定的端口
     */
    public static final String CONF_RPC_SERVER_PORT = "rpc.server.port";
    /**
     * RPC Server的处理线程池的个数
     */
    public static final String CONF_RPC_SERVER_THREADPOOL_SIZE = "rpc.server.threadpool.size";
    /**
     * rpc服务zookeeper命名服务上的注册根路径
     */
    public static final String CONF_RPC_SERVICE_ZK_ROOT = "rpc.servcie.zk.root";

    private RPCConfig() {

    }
}

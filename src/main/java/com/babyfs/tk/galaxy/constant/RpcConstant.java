package com.babyfs.tk.galaxy.constant;

public class RpcConstant {
    public static final String ZK_REGISTER_ROOT = "rpc.zk.register.root";

    public static final String ZK_REGISTER_ROOT_DEFAULT = "/galaxy/register";

    public static final String SERVER_IP = "rpc.server.ip";

    public static final String SERVER_PORT = "rpc.server.port";

    public static final String ZK_CONNECT_TIMEOUT = "rpc.zk.connect.timeout";

    public static final int ZK_CONNECT_TIMEOUT_DEFAULT = 5000;

    public static final String ZK_SESSION_TIMEOUT = "rpc.zk.session.timeout";

    public static final int ZK_SESSION_TIMEOUT_DEFAULT = 10000;

    public static final String RPC_URL_PREFIX_DEFAULT = "/internal/rpc/invoke";


    public static final String ZK_BOOTSTRAP_SERVERS = "rpc.zk.bootstrap.servers";

    public static final String ZK_BOOTSTRAP_SERVERS_DEFAULT = "127.0.0.1:2181";

    /**
     * rpc客户端调用前缀
     */
    public static final String NAME_RPC_CLIENT_URL_PREFIX = "rpc.client.url.prefixe";
    /**
     * rpc服务端暴露出来的服务
     */
    public static final String NAME_RPC_SERVER_EXPOSE = "rpc.server.expose";

    //连接超时时间 单位秒
    public static final String OK_CONNECT_TIMEOUT = "ok.http.connect.timeout";
    //读超时时间 单位秒
    public static final String OK_READ_TIMEOUT = "ok.http.read.timeout";
    //写超时时间 单位秒
    public static final String OK_WRITE_TIMEOUT = "ok.http.write.timeout";
}

package com.babyfs.tk.galaxy.constant;

public class RpcConstant {

    public static final String DISCOVERY_PREFIX = "/galaxy/discovery";

    public static final String APP_NAME = "app.name";

    public static final String APP_PORT = "app.port";

    public static final int APP_PORT_DEFAULT = 8080;

    public static final String APP_CONNECT_TIMEOUT = "app.connect.timeout";

    public static final int APP_CONNECT_TIMEOUT_DEFAULT = 15000;

    public static final String APP_SESSION_TIMEOUT = "app.session.timeout";

    public static final int APP_SESSION_TIMEOUT_DEFAULT = 15000;

    public static final String RPC_URL_PREFIX_DEFAULT = "/internal/rpc/invoke";

    public static final String ZK_BOOTSTRAP_SERVERS = "zk.bootstrap.servers";

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
    public static final String CONNECT_TIMEOUT = "ok.http.connect.timeout";
    //读超时时间 单位秒
    public static final String READ_TIMEOUT = "ok.http.read.timeout";
    //写超时时间 单位秒
    public static final String WRITE_TIMEOUT = "ok.http.write.timeout";
}

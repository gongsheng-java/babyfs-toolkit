package com.babyfs.tk.http.constants;

/**
 * Async HTTP client的配置项
 * <p/>
 */
public final class AsyncHttpClientConfig {
    private AsyncHttpClientConfig() {

    }

    /** Async Http client 最大连接数 */
    public static final String CONF_ASYNC_HTTP_CLIENT_MAX_CONNECTION = "async.http.client.max.connection.num";

    /** Async Http client 连接超时 */
    public static final String CONF_ASYNC_HTTP_CLIENT_CONNECTION_TIMEOUT = "async.http.client.connection.timeout";

    /** Async Http client socket超时 */
    public static final String CONF_ASYNC_HTTP_CLIENT_SOCKET_TIMEOUT = "async.http.client.socket.timeout";

    /** Async Http client 是否使用代理 */
    public static final String CONF_ASYNC_HTTP_CLIENT_PROXY_ISUSE = "async.http.client.proxy.isuse";

    /** Async Http client 代理用户名 */
    public static final String CONF_ASYNC_HTTP_CLIENT_PROXY_USER = "async.http.client.proxy.user";

    /** Async Http client 代理密码 */
    public static final String CONF_ASYNC_HTTP_CLIENT_PROXY_PASSWD = "async.http.client.proxy.passwd";

    /** Async Http client 代理Host */
    public static final String CONF_ASYNC_HTTP_CLIENT_PROXY_HOST = "async.http.client.proxy.host";

    /** Async Http client 代理端口 */
    public static final String CONF_ASYNC_HTTP_CLIENT_PROXY_PORT = "async.http.client.proxy.port";

}

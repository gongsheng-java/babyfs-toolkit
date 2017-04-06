package com.babyfs.tk.http.client;

/**
 * HTTP客户端代理相关配置包装类
 * <p/>
 */
public final class HttpClientProxyConfig {

    /** 是否开启代理 */
    private final boolean useProxy;
    /** 代理主机IP */
    private final String proxyHost;
    /** 代理主机端口 */
    private final int proxyPort;
    /** 代理授权用户名 */
    private final String proxyAuthUser;
    /** 代理授权密码 */
    private final String proxyAuthPassword;

    public HttpClientProxyConfig(boolean useProxy, String proxyHost, int proxyPort,
                                 String proxyAuthUser, String proxyAuthPassword) {
        this.useProxy = useProxy;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.proxyAuthUser = proxyAuthUser;
        this.proxyAuthPassword = proxyAuthPassword;
    }

    public boolean isUseProxy() {
        return useProxy;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public String getProxyAuthUser() {
        return proxyAuthUser;
    }

    public String getProxyAuthPassword() {
        return proxyAuthPassword;
    }
}

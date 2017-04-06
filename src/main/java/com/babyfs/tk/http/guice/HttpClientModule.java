package com.babyfs.tk.http.guice;


import com.google.inject.Inject;
import com.google.inject.PrivateModule;
import com.google.inject.Provider;
import com.babyfs.tk.commons.MapConfig;
import com.babyfs.tk.commons.config.IConfigService;
import com.babyfs.tk.http.client.HttpClientService;
import com.babyfs.tk.http.constants.HttpClientConfig;

/**
 * 用于创建HTTP客户端的模块
 * <p/>
 */
public class HttpClientModule extends PrivateModule {

    public static final int DEFAULT_MAX_CONNECTION = 200;
    public static final int DEFAULT_CONNECTION_TIMEOUT = 5000;
    public static final int DEFAULT_SOCKET_TIMEOUT = 5000;

    public HttpClientModule() {
    }

    @Override
    protected void configure() {
        bind(HttpClientService.class).toProvider(HttpClientServiceProvider.class).asEagerSingleton();
        expose(HttpClientService.class); //导出给其他业务模块
    }

    /**
     * HTTP客户端服务的提供者
     */
    private static class HttpClientServiceProvider implements Provider<HttpClientService> {

        @Inject(optional = true)
        private IConfigService conf;

        @Override
        public HttpClientService get() {
            int maxConnection = MapConfig.getInt(HttpClientConfig.CONF_HTTP_CLIENT_MAX_CONNECTION,
                    conf, DEFAULT_MAX_CONNECTION);
            int connectTimeout = MapConfig.getInt(HttpClientConfig.CONF_HTTP_CLIENT_CONNECTION_TIMEOUT,
                    conf, DEFAULT_CONNECTION_TIMEOUT);
            int socketTimeout = MapConfig.getInt(HttpClientConfig.CONF_HTTP_CLIENT_SOCKET_TIMEOUT,
                    conf, DEFAULT_SOCKET_TIMEOUT);
            return new HttpClientService(maxConnection, connectTimeout, socketTimeout);
        }
    }

}

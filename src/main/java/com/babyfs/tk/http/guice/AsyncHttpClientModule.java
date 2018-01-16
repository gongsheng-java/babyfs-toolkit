package com.babyfs.tk.http.guice;


import com.babyfs.tk.commons.service.IStageActionRegistry;
import com.babyfs.tk.commons.service.annotation.ShutdownStage;
import com.google.inject.Inject;
import com.google.inject.PrivateModule;
import com.google.inject.Provider;
import com.babyfs.tk.commons.MapConfig;
import com.babyfs.tk.commons.config.IConfigService;
import com.babyfs.tk.http.client.AsyncHttpClientService;
import com.babyfs.tk.http.client.HttpClientProxyConfig;
import com.babyfs.tk.http.constants.AsyncHttpClientConfig;

/**
 * 用于创建AsyncHTTP客户端的模块(异步)
 * <p/>
 */
public class AsyncHttpClientModule extends PrivateModule {

    public static final int DEFAULT_MAX_CONNECTION = 200;
    public static final int DEFAULT_CONNECTION_TIMEOUT = 5000;
    public static final int DEFAULT_SOCKET_TIMEOUT = 5000;

    public AsyncHttpClientModule() {
    }

    @Override
    protected void configure() {
        bind(AsyncHttpClientService.class).toProvider(AsyncHttpClientServiceProvider.class).asEagerSingleton();
        expose(AsyncHttpClientService.class); //导出给其他业务模块
    }

    /**
     * HTTP客户端服务的提供者
     */
    private static class AsyncHttpClientServiceProvider implements Provider<AsyncHttpClientService> {
        @Inject(optional = true)
        private IConfigService conf;

        @Inject(optional = true)
        @ShutdownStage
        private IStageActionRegistry registry;

        @Override
        public AsyncHttpClientService get() {
            int maxConnection = MapConfig.getInt(AsyncHttpClientConfig.CONF_ASYNC_HTTP_CLIENT_MAX_CONNECTION,
                    conf, DEFAULT_MAX_CONNECTION);
            int connectTimeout = MapConfig.getInt(AsyncHttpClientConfig.CONF_ASYNC_HTTP_CLIENT_CONNECTION_TIMEOUT,
                    conf, DEFAULT_CONNECTION_TIMEOUT);
            int socketTimeout = MapConfig.getInt(AsyncHttpClientConfig.CONF_ASYNC_HTTP_CLIENT_SOCKET_TIMEOUT,
                    conf, DEFAULT_SOCKET_TIMEOUT);
            boolean isUse = MapConfig.getBoolean(AsyncHttpClientConfig.CONF_ASYNC_HTTP_CLIENT_PROXY_ISUSE,
                    conf, false);
            String host = MapConfig.getString(AsyncHttpClientConfig.CONF_ASYNC_HTTP_CLIENT_PROXY_HOST,
                    conf, "");
            String passwd = MapConfig.getString(AsyncHttpClientConfig.CONF_ASYNC_HTTP_CLIENT_PROXY_PASSWD,
                    conf, "");
            String user = MapConfig.getString(AsyncHttpClientConfig.CONF_ASYNC_HTTP_CLIENT_PROXY_USER,
                    conf, "");
            int port = MapConfig.getInt(AsyncHttpClientConfig.CONF_ASYNC_HTTP_CLIENT_PROXY_PORT,
                    conf, 0);

            HttpClientProxyConfig httpClientProxyConfig = new HttpClientProxyConfig(isUse, host, port, user, passwd);
            AsyncHttpClientService asyncHttpClientService = new AsyncHttpClientService(maxConnection, connectTimeout, socketTimeout, httpClientProxyConfig);
            if (registry != null) {
                registry.addAction(asyncHttpClientService::stop);
            }
            return asyncHttpClientService;
        }
    }

}

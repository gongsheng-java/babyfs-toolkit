package com.babyfs.tk.galaxy.client;

import com.babyfs.tk.galaxy.RpcException;
import org.apache.commons.io.IOUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.CodingErrorAction;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;


public enum RpcHttpClient implements Client {

    http;
    private static final int DEFAULT_CONNECTION_MANAGER_TIME_OUT = 5000;
    // 默认的连接等待时间
    private static final int DEFAULT_CONNECTION_TIME_OUT = 5000;
    // 默认的数据等待时间
    private static final int DEFAULT_SOCKET_TIME_OUT = 5000;
    private static final int DEFAULT_MAX_REDIRECTS = 5;
    private static final int DEFAULT_MAX_CONNECT = 1000;
    private static final int DEFAULT_MAX_CONNECT_PER_ROUTE = 50;
    private static final long DEFAULT_TIME_TO_LIVE = 10000l;
    private CloseableHttpClient httpClient;

    private RpcHttpClient() {
        init();
    }

    private void init() {

        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .build();
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry,
                null, null, SystemDefaultDnsResolver.INSTANCE, DEFAULT_TIME_TO_LIVE, TimeUnit.MILLISECONDS);
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setCharset(Consts.UTF_8)
                .setMalformedInputAction(CodingErrorAction.IGNORE)
                .setUnmappableInputAction(CodingErrorAction.IGNORE)
                .build();
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(DEFAULT_CONNECTION_TIME_OUT)
                .setSocketTimeout(DEFAULT_SOCKET_TIME_OUT)
                .setMaxRedirects(DEFAULT_MAX_REDIRECTS)
                .setConnectionRequestTimeout(DEFAULT_CONNECTION_MANAGER_TIME_OUT)
                .build();
        SocketConfig socketConfig = SocketConfig.custom()
                .setTcpNoDelay(true)
                .setSoReuseAddress(true)
                .build();
        connectionManager.setMaxTotal(DEFAULT_MAX_CONNECT);
        connectionManager.setDefaultMaxPerRoute(DEFAULT_MAX_CONNECT_PER_ROUTE);
        connectionManager.setDefaultConnectionConfig(connectionConfig);
        connectionManager.setDefaultSocketConfig(socketConfig);
        connectionManager.closeExpiredConnections();
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        httpClientBuilder.setConnectionManager(connectionManager);
        httpClientBuilder.setDefaultRequestConfig(requestConfig)
                .setConnectionReuseStrategy(new DefaultConnectionReuseStrategy())
                .setRetryHandler(new DefaultHttpRequestRetryHandler(0, false))
                .setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy());
        httpClient = httpClientBuilder.build();
    }


    private CloseableHttpResponse internalDoHttpRequest(String uri, byte[] body) throws IOException {

        RequestBuilder requestBuilder = RequestBuilder.post();
        requestBuilder.setUri(uri);
        HttpEntity entity = EntityBuilder.create().setContentType(ContentType.DEFAULT_BINARY).setBinary(body).setContentEncoding("utf-8").build();
        requestBuilder.setEntity(entity);
        return httpClient.execute(requestBuilder.build());
    }

    public byte[] execute(String uri, byte[] body) throws IOException {

        CloseableHttpResponse response = internalDoHttpRequest(uri, body);
        return handlerResponse(response);
    }

    private byte[] handlerResponse(CloseableHttpResponse response) throws ParseException, IOException {

        try {
            int status = response.getStatusLine().getStatusCode();
            if (status != 200) {
                throw new RpcException(format("error status(%s) ", status));
            }
            return EntityUtils.toByteArray(response.getEntity());

        } finally {
            IOUtils.closeQuietly(response);
        }
    }


}

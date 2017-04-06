package com.babyfs.tk.http.client;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.io.CharStreams;
import com.ning.http.client.*;
import com.babyfs.tk.commons.Constants;
import com.babyfs.tk.http.constants.HttpClientConfig;
import org.apache.http.HttpStatus;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Map;


/**
 * AsyncHTTP 客户端服务(异步)
 * <p/>
 */
public class AsyncHttpClientService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncHttpClientService.class);
    /**
     * Async HTTP 客户端
     */
    private AsyncHttpClient httpClient;

    /**
     * 默认主机最大连接数
     */
    private static final int DEFAULT_MAX_CON_PER_HOST = 100;
    /**
     * 默认连接超时毫秒数
     */
    private static final int DEFAULT_CON_TIME_OUT_MS = 1000;
    /**
     * 默认套接字超时毫秒数
     */
    private static final int DEFAULT_SO_TIME_OUT_MS = 1000;

    /**
     * 使用默认值构造HTTP客户端服务<br/>
     * </p>
     * 默认主机最大连接数：{@link AsyncHttpClientService#DEFAULT_MAX_CON_PER_HOST} <br/>
     * 默认连接超时毫秒数：{@link AsyncHttpClientService#DEFAULT_CON_TIME_OUT_MS} <br/>
     * 默认套接字超时毫秒数：{@link AsyncHttpClientService#DEFAULT_SO_TIME_OUT_MS}  <br/>
     */
    public AsyncHttpClientService() {
        this(DEFAULT_MAX_CON_PER_HOST, DEFAULT_CON_TIME_OUT_MS, DEFAULT_SO_TIME_OUT_MS);
    }

    /**
     * 构造HTTP客户端服务，默认不开启代理，如果需要使用代理功能应调用：<br/>
     * {@link AsyncHttpClientService#AsyncHttpClientService(int, int, int, HttpClientProxyConfig)}
     *
     * @param maxConPerHost 主机最大连接数
     * @param conTimeOutMs  连接超时毫秒数
     * @param soTimeOutMs   套接字超时毫秒数
     */
    public AsyncHttpClientService(final int maxConPerHost, final int conTimeOutMs, final int soTimeOutMs) {
        // 多线程连接管理器
        this(maxConPerHost, conTimeOutMs, soTimeOutMs, null);
    }

    /**
     * 构造提供代理功能的HTTP客户端服务，代理参数参见{@link HttpClientProxyConfig}
     *
     * @param maxConPerHost 主机最大连接数
     * @param conTimeOutMs  连接超时毫秒数
     * @param soTimeOutMs   套接字超时毫秒数
     * @param proxyConfig   代理配置参数
     */
    public AsyncHttpClientService(final int maxConPerHost, final int conTimeOutMs,
                                  final int soTimeOutMs, final HttpClientProxyConfig proxyConfig) {

        AsyncHttpClientConfig.Builder builder = new AsyncHttpClientConfig.Builder();
        builder.setMaximumConnectionsPerHost(maxConPerHost)
                .setConnectionTimeoutInMs(conTimeOutMs)
                .setRequestTimeoutInMs(soTimeOutMs);
        if (proxyConfig != null && proxyConfig.isUseProxy()) {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(proxyConfig.getProxyHost()),
                    "proxy host must not be null or empty.");
            Preconditions.checkArgument(proxyConfig.getProxyPort() > 0, "proxy port must be larger than 0.");
            ProxyServer proxyServer = new ProxyServer(proxyConfig.getProxyHost(), proxyConfig.getProxyPort(), proxyConfig.getProxyAuthUser(), proxyConfig.getProxyAuthPassword());
            builder.setUseProxyProperties(proxyConfig.isUseProxy())
                    .setProxyServer(proxyServer);
        }
        this.httpClient = new AsyncHttpClient(builder.build());
    }


    /**
     * 发送get请求
     *
     * @param url       请求的URL
     * @param getParams GET请求参数
     * @param headers   请求头部参数
     * @return
     */
    public String sendGet(String url, FluentStringsMap getParams, Map<String, Collection<String>> headers) throws Exception {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(url), "URL can not be empty or null.");
        LOGGER.debug("Get Request:{}", url);
        AsyncHttpClient.BoundRequestBuilder boundRequestBuilder = httpClient.prepareGet(url)
                .setBodyEncoding(Constants.DEFAULT_CHARSET)
                .setQueryParameters(getParams)
                .setHeaders(headers);
        return sendRequest(boundRequestBuilder);
    }

    /**
     * 发送post请求
     *
     * @param url       请求的URL
     * @param getParams POST请求参数
     * @param headers   请求头部参数
     * @return
     */
    public String sendPost(String url, Map<String, Collection<String>> getParams, Map<String, Collection<String>> headers) throws Exception {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(url), "URL can not be empty or null.");
        LOGGER.debug("Post Request:{}", url);
        AsyncHttpClient.BoundRequestBuilder boundRequestBuilder = httpClient.preparePost(url);
        boundRequestBuilder.setBodyEncoding("UTF-8");
        boundRequestBuilder.setHeaders(headers);
        boundRequestBuilder.setParameters(getParams);
        return sendRequest(boundRequestBuilder);
    }


    /**
     * 发送指定Http方法的请求，并接收response的字符串
     * <p/>
     * 对于服务器返回的压缩数据的情况，目前支持gzip和deflate两种解压缩方式
     *
     * @param boundRequestBuilder GET或POST方法
     * @return 返回response字符串
     * @throws java.io.IOException
     * @throws IllegalStateException 对于gzip和deflate以外压缩格式，抛出该异常
     */
    private String sendRequest(AsyncHttpClient.BoundRequestBuilder boundRequestBuilder) throws Exception {
        ListenableFuture<Response> listenableFuture = null;
        try {
            listenableFuture = boundRequestBuilder.execute();
            Response response = listenableFuture.get();
            int statusCode = response.getStatusCode();
            LOGGER.debug("Response return statusCode:{}", statusCode);

            if (statusCode != HttpStatus.SC_OK) {
                throw new IllegalStateException("Http response error. status code:" + statusCode);
            }

            if (LOGGER.isDebugEnabled()) {
                FluentCaseInsensitiveStringsMap resHeader = response.getHeaders();
                LOGGER.debug("response headers :");
            }

            String encodingHeader = response.getHeader(HttpHeaders.Names.CONTENT_ENCODING);
            if (!Strings.isNullOrEmpty(encodingHeader)) {
                if (HttpClientConfig.CompressFormat.COMPRESS_FORMAT_GZIP.isBelong(encodingHeader)) {
                    return HttpClientUtils.uncompressStream(HttpClientConfig.CompressFormat.COMPRESS_FORMAT_GZIP, response.getResponseBodyAsStream(), null);
                } else if (HttpClientConfig.CompressFormat.COMPRESS_FORMAT_DEFLATE.isBelong(encodingHeader)) {
                    return HttpClientUtils.uncompressStream(HttpClientConfig.CompressFormat.COMPRESS_FORMAT_DEFLATE, response.getResponseBodyAsStream(), null);
                } else if (HttpClientConfig.CompressFormat.COMPRESS_FORMAT_IDENTITY.isBelong(encodingHeader)) {
                    return response.getResponseBody();
                } else {
                    LOGGER.error("Unsupported HTTP compressed encoding:{}", encodingHeader);
                    throw new RuntimeException("Unsupported HTTP compressed encoding:" + encodingHeader);
                }
            }
            // 如果response头部没有指示编码格式，认为非压缩，直接返回
            InputStreamReader inputReader = new InputStreamReader(response.getResponseBodyAsStream(), Constants.DEFAULT_CHARSET);
            return CharStreams.toString(inputReader);
        } finally {
            /// 不论如何，释放连接
//            if (listenableFuture != null && listenableFuture.isDone()) {
//                listenableFuture.cancel(true);
//            }
        }
    }
}

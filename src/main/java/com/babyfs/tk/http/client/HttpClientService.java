package com.babyfs.tk.http.client;

import com.babyfs.tk.commons.Constants;
import com.babyfs.tk.commons.base.Pair;
import com.babyfs.tk.commons.base.Tuple;
import com.babyfs.tk.http.constants.HttpClientConfig;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.net.MediaType;
import org.apache.http.*;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Map;


/**
 * HTTP 客户端服务
 * <p/>
 */
public class HttpClientService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientService.class);
    protected static final ContentType TEXT_UTF8 = ContentType.create("text/plain", Consts.UTF_8);
    /**
     * Apache HTTP 客户端
     */
    private CloseableHttpClient httpClient;

    /**
     * 默认主机最大连接数
     */
    private static final int DEFAULT_MAX_CON_PER_HOST = 100;
    /**
     * 默认连接超时毫秒数
     */
    private static final int DEFAULT_CON_TIME_OUT_MS = 3000;
    /**
     * 默认套接字超时毫秒数
     */
    private static final int DEFAULT_SO_TIME_OUT_MS = 3000;

    /**
     * 使用默认值构造HTTP客户端服务<br/>
     * </p>
     * 默认主机最大连接数：{@link HttpClientService#DEFAULT_MAX_CON_PER_HOST} <br/>
     * 默认连接超时毫秒数：{@link HttpClientService#DEFAULT_CON_TIME_OUT_MS} <br/>
     * 默认套接字超时毫秒数：{@link HttpClientService#DEFAULT_SO_TIME_OUT_MS}  <br/>
     */
    public HttpClientService() {
        this(DEFAULT_MAX_CON_PER_HOST, DEFAULT_CON_TIME_OUT_MS, DEFAULT_SO_TIME_OUT_MS);
    }

    /**
     * 构造HTTP客户端服务，默认不开启代理，如果需要使用代理功能应调用：<br/>
     * {@link HttpClientService#HttpClientService(int, int, int, HttpClientProxyConfig)}
     *
     * @param maxConPerHost 主机最大连接数
     * @param conTimeOutMs  连接超时毫秒数
     * @param soTimeOutMs   套接字超时毫秒数
     */
    public HttpClientService(final int maxConPerHost, final int conTimeOutMs, final int soTimeOutMs) {
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
    public HttpClientService(final int maxConPerHost, final int conTimeOutMs,
                             final int soTimeOutMs, final HttpClientProxyConfig proxyConfig) {

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setDefaultMaxPerRoute(maxConPerHost);
        SocketConfig socketConfig = SocketConfig.custom()
                .setTcpNoDelay(true)
                .setSoTimeout(soTimeOutMs)
                .setSoReuseAddress(true)
                .build();
        connectionManager.setDefaultSocketConfig(socketConfig);

        RequestConfig.Builder requestBilder = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                .setExpectContinueEnabled(true)
                .setConnectTimeout(conTimeOutMs)
                .setStaleConnectionCheckEnabled(true);

        HttpClientBuilder httpClientBuilder = HttpClients.custom();


        if (proxyConfig != null && proxyConfig.isUseProxy()) {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(proxyConfig.getProxyHost()),
                    "proxy host must not be null or empty.");
            Preconditions.checkArgument(proxyConfig.getProxyPort() > 0, "proxy port must be larger than 0.");
            requestBilder.setProxy(new HttpHost(proxyConfig.getProxyHost(), proxyConfig.getProxyPort()));
            LOGGER.info("Proxy host: {}", proxyConfig.getProxyHost());
            LOGGER.info("Proxy port: {}", proxyConfig.getProxyPort());
            //代理授权用户名和密码可以为空，即该代理不需要用户名密码认证
            if (!Strings.isNullOrEmpty(proxyConfig.getProxyAuthUser())) {
                CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(proxyConfig.getProxyAuthUser(), proxyConfig.getProxyAuthPassword()));
                requestBilder.setAuthenticationEnabled(true);
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                LOGGER.info("Proxy auth user name: {}", proxyConfig.getProxyAuthUser());
                LOGGER.info("Proxy auth password: {}", proxyConfig.getProxyAuthPassword());
            }
        }

        httpClient = httpClientBuilder
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestBilder.build())
                .build();
    }

    public synchronized void close() {
        if (this.httpClient != null) {
            try {
                this.httpClient.close();
            } catch (IOException e) {
                LOGGER.error("Close httpClient error", e);
            }
            this.httpClient = null;
        }
    }


    /**
     * 发送get请求
     *
     * @param url       请求的URL
     * @param getParams GET请求参数
     * @param headers   请求头部参数
     * @return
     */
    public String sendGet(String url, Map<String, String> getParams, Map<String, String> headers) throws IOException {
        return sendGet(url, getParams, headers, null);
    }

    /**
     * 发送get请求
     *
     * @param url            请求的URL
     * @param getParams      GET请求参数
     * @param headers        请求头部参数
     * @param defaultCharset 默认的响应编码
     * @return
     */
    public String sendGet(String url, Map<String, String> getParams, Map<String, String> headers, String defaultCharset) throws IOException {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(url), "URL can not be empty or null.");
        LOGGER.debug("Get Request:{}", url);
        try {
            HttpGet get = new HttpGet(HttpClientUtils.assemblyGetURI(url, getParams));
            HttpClientUtils.assemblyHeaders(get, headers);
            return sendRequest(get, defaultCharset);
        } catch (URISyntaxException ue) {
            throw new RuntimeException(ue);
        }
    }

    /**
     * 发送post请求
     *
     * @param url        请求的URL
     * @param postParams POST请求参数
     * @param headers    请求头部参数
     * @return
     */
    public String sendPost(String url, Map<String, String> postParams, Map<String, String> headers) throws IOException {
        return this.sendPost(url, postParams, headers, (String) null);
    }

    /**
     * 发送post请求
     *
     * @param url            请求的URL
     * @param postParams     POST请求参数
     * @param headers        请求头部参数
     * @param defaultCharset 默认的响应编码
     * @return
     */
    public String sendPost(String url, Map<String, String> postParams, Map<String, String> headers, String defaultCharset) throws IOException {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(url), "URL can not be empty or null.");
        LOGGER.debug("Post Request:{}", url);
        HttpPost post = new HttpPost(url);
        HttpClientUtils.assemblyPostParams(post, postParams);
        HttpClientUtils.assemblyHeaders(post, headers);
        return sendRequest(post, defaultCharset);
    }

    /**
     * 发送post请求
     *
     * @param url      请求的URL
     * @param headers  请求头部参数
     * @param bodyData post body
     * @return
     */
    public String sendPostBody(String url, Map<String, String> headers, byte[] bodyData) throws IOException {
        return this.sendPostBody(url, headers, bodyData, null);
    }

    /**
     * 发送post请求
     *
     * @param url            请求的URL
     * @param headers        请求头部参数
     * @param bodyData       post body
     * @param defaultCharset 默认的响应编码
     * @return
     */
    public String sendPostBody(String url, Map<String, String> headers, byte[] bodyData, String defaultCharset) throws IOException {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(url), "URL can not be empty or null.");
        LOGGER.debug("Post Request:{}", url);
        HttpPost post = new HttpPost(url);
        HttpClientUtils.assemblyHeaders(post, headers);
        post.setEntity(new ByteArrayEntity(bodyData));
        return sendRequest(post, defaultCharset);
    }

    /**
     * 发送post请求
     *
     * @param url         请求的URL
     * @param headers     请求头部参数
     * @param inputStream 文件流
     * @return
     */
    public String sendPostBody(String url, Map<String, String> headers, InputStream inputStream) throws IOException {
        return this.sendPostBody(url, headers, inputStream, null);
    }

    /**
     * 发送post请求
     *
     * @param url            请求的URL
     * @param headers        请求头部参数
     * @param inputStream    文件流
     * @param defaultCharset 默认的响应编码
     * @return
     */
    public String sendPostBody(String url, Map<String, String> headers, InputStream inputStream, String defaultCharset) throws IOException {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(url), "URL can not be empty or null.");
        LOGGER.debug("Post Request:{}", url);
        HttpPost post = new HttpPost(url);
        HttpClientUtils.assemblyHeaders(post, headers);
        post.setEntity(new InputStreamEntity(inputStream));
        return sendRequest(post, defaultCharset);
    }

    /**
     * @param url
     * @param postParams
     * @param headers
     * @param binaries
     * @return
     * @throws IOException
     */
    public String sendPost(String url, Map<String, String> postParams, Map<String, String> headers, Tuple<String, String, InputStream>... binaries) throws IOException {
        return this.sendPost(url, postParams, headers, null, binaries);
    }

    /**
     * @param url
     * @param postParams
     * @param headers
     * @param defaultCharset
     * @param binaries
     * @return
     * @throws IOException
     */
    public String sendPost(String url, Map<String, String> postParams, Map<String, String> headers, String defaultCharset, Tuple<String, String, InputStream>... binaries) throws IOException {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(url), "URL can not be empty or null.");
        HttpPost post = new HttpPost(url);
        HttpClientUtils.assemblyHeaders(post, headers);
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        if (postParams != null) {
            for (Map.Entry<String, String> entry : postParams.entrySet()) {
                multipartEntityBuilder.addTextBody(entry.getKey(), entry.getValue(), TEXT_UTF8);
            }
        }
        for (Tuple<String, String, InputStream> tuple : binaries) {
            multipartEntityBuilder.addBinaryBody(tuple.getFirst(), tuple.getThird(), ContentType.DEFAULT_BINARY, tuple.getSecond());
        }
        HttpEntity entity = multipartEntityBuilder.build();
        post.setEntity(entity);
        return sendRequest(post, defaultCharset);
    }

    /**
     * 发送指定Http方法的请求，并接收response的字符串
     * <p/>
     * 对于服务器返回的压缩数据的情况，目前支持gzip和deflate两种解压缩方式
     *
     * @param method         GET或POST方法
     * @param defaultCharset 默认的响应编码
     * @return 返回response字符串
     * @throws IOException
     */
    private String sendRequest(HttpRequestBase method, String defaultCharset) throws IOException{
        CloseableHttpResponse closeableHttpResponse = null;
        try {
            closeableHttpResponse = httpClient.execute(method);
            int statusCode = closeableHttpResponse.getStatusLine().getStatusCode();
            LOGGER.debug("Response return statusCode:{}", statusCode);

            String responseContent = getResponseContent(closeableHttpResponse, defaultCharset);
            if (statusCode != HttpStatus.SC_OK) {
                throw new IOException("Http response error. status code:" + statusCode + " uri:" + method.getURI() + " resp:" + responseContent);
            }

            return responseContent;
        } finally {
            // 不论如何，释放连接
            method.releaseConnection();
            if (closeableHttpResponse != null) {
                closeableHttpResponse.close();
            }

        }
    }

    /**
     * 发送get请求,返回response的二进制流,
     * <p/>
     * 主要应用于图片下载，  返回数据无压缩
     *
     * @param url       请求的URL
     * @param getParams GET请求参数
     * @param headers   GET请求头
     * @return 返回contentType和response二进制流
     */
    public Pair<String, byte[]> sendGetForRaw(String url, Map<String, String> getParams, Map<String, String> headers) throws IOException {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(url), "URL can not be empty or null.");
        LOGGER.info("Get Request:{}", url);
        try {
            HttpGet get = new HttpGet(HttpClientUtils.assemblyGetURI(url, getParams));
            HttpClientUtils.assemblyHeaders(get, headers);
            return sendRequestForRaw(get);
        } catch (URISyntaxException ue) {
            throw new RuntimeException(ue);
        }
    }

    /**
     * 发送post请求,返回response的二进制流,
     * <p/>
     * 主要应用于图片下载, 返回数据无压缩
     *
     * @param url        请求的URL
     * @param postParams POST请求参数
     * @return 返回contentType和response二进制流
     */

    public Pair<String, byte[]> sendPostForRaw(String url, Map<String, String> postParams) throws IOException {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(url), "URL can not be empty or null.");
        LOGGER.info("Post Request:{}", url);
        HttpPost post = new HttpPost(url);
        HttpClientUtils.assemblyPostParams(post, postParams);
        HttpClientUtils.assemblyHeaders(post, null);
        return sendRequestForRaw(post);
    }

    /**
     * 发送MutilPart请求
     *
     * @param url     请求的URL
     * @param headers 请求头
     * @param params  参数
     * @param streams key,{@link Pair#getFirst()} 字段名,{@link Pair#getSecond()} 文件名
     * @return
     * @throws IOException
     */
    public String sendMultipartPost(String url, Map<String, String> headers, Map<String, String> params, Map<Pair<String, String>, InputStream> streams) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        MultipartEntityBuilder multiPartBuilder = MultipartEntityBuilder.create();
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                String name = entry.getKey();
                String value = entry.getValue();
                httpPost.setHeader(name, value);
            }
        }

        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String name = entry.getKey();
                String value = entry.getValue();
                if (!Strings.isNullOrEmpty(name) && !Strings.isNullOrEmpty(value)) {
                    multiPartBuilder.addPart(name, new StringBody(value, TEXT_UTF8));
                    multiPartBuilder.addPart(name, new StringBody(value, TEXT_UTF8));
                }
            }
        }

        if (streams != null) {
            for (Map.Entry<Pair<String, String>, InputStream> entry : streams.entrySet()) {
                String fieldName = entry.getKey().getFirst();
                String fileName = entry.getKey().getSecond();
                multiPartBuilder.addBinaryBody(fieldName, entry.getValue(), ContentType.DEFAULT_BINARY, fileName);
            }
        }

        HttpEntity entity = multiPartBuilder.build();
        httpPost.setEntity(entity);
        return this.sendRequest(httpPost, Constants.UTF_8);
    }

    /**
     * 发送指定Http方法的请求，并接收response的二进制流
     * <p/>
     * 对于服务器返回的数据不压缩
     *
     * @param method GET或POST方法
     * @return 返回contentType和response二进制流
     * @throws IOException
     */
    private Pair<String, byte[]> sendRequestForRaw(HttpRequestBase method) throws IOException {
        CloseableHttpResponse closeableHttpResponse = null;
        try {
            closeableHttpResponse = httpClient.execute(method);
            int statusCode = closeableHttpResponse.getStatusLine().getStatusCode();
            LOGGER.debug("Response return statusCode:{}", statusCode);
            if (statusCode != HttpStatus.SC_OK) {
                throw new IOException("Http response error. status code:" + statusCode);
            }

            String contentType = null;
            Header contentTypeHeader = closeableHttpResponse.getFirstHeader(HttpHeaders.CONTENT_TYPE);
            if (contentTypeHeader != null) {
                contentType = contentTypeHeader.getValue();
            }

            return Pair.of(contentType, EntityUtils.toByteArray(closeableHttpResponse.getEntity()));
        } finally {
            // 不论如何，释放连接
            method.releaseConnection();
            if (closeableHttpResponse != null) {
                closeableHttpResponse.close();
            }
        }
    }

    private String getResponseContent(CloseableHttpResponse closeableHttpResponse, String defaultCharset) throws IOException {
        HttpEntity entity = closeableHttpResponse.getEntity();
        if (entity == null) {
            return null;
        }

        String charset = null;
        if (defaultCharset != null) {
            charset = defaultCharset;
        } else {
            Header contentTypeHeader = closeableHttpResponse.getFirstHeader(HttpHeaders.CONTENT_TYPE);
            if (contentTypeHeader != null) {
                String contentType = contentTypeHeader.getValue();
                if (!Strings.isNullOrEmpty(contentType)) {
                    MediaType mediaType = HttpClientUtils.parseMediaType(contentType);
                    if (mediaType != null) {
                        Optional<Charset> charsetOptional = mediaType.charset();
                        if (charsetOptional.isPresent()) {
                            charset = charsetOptional.get().name();
                        }
                    }
                }
            }
        }

        if (Strings.isNullOrEmpty(charset)) {
            charset = Constants.UTF_8;
        }

        Header encodingHeader = closeableHttpResponse.getFirstHeader(HttpHeaders.CONTENT_ENCODING);
        if (encodingHeader != null) {
            if (HttpClientConfig.CompressFormat.COMPRESS_FORMAT_GZIP.isBelong(encodingHeader.getValue())) {
                return HttpClientUtils.uncompressStream(HttpClientConfig.CompressFormat.COMPRESS_FORMAT_GZIP, entity.getContent(), charset);
            } else if (HttpClientConfig.CompressFormat.COMPRESS_FORMAT_DEFLATE.isBelong(encodingHeader.getValue())) {
                return HttpClientUtils.uncompressStream(HttpClientConfig.CompressFormat.COMPRESS_FORMAT_DEFLATE, entity.getContent(), charset);
            } else if (HttpClientConfig.CompressFormat.COMPRESS_FORMAT_IDENTITY.isBelong(encodingHeader.getValue())) {
                return EntityUtils.toString(entity, charset);
            } else {
                LOGGER.error("Unsupported HTTP compressed encoding:{}", encodingHeader.getValue());
                throw new RuntimeException("Unsupported HTTP compressed encoding:" + encodingHeader.getValue());
            }
        }
        // 如果response头部没有指示编码格式，认为非压缩，直接返回
        return EntityUtils.toString(entity, charset);
    }

    private byte[] getRawResponse(CloseableHttpResponse closeableHttpResponse) throws IOException {
        HttpEntity entity = closeableHttpResponse.getEntity();
        if (entity == null) {
            return null;
        }


        Header encodingHeader = closeableHttpResponse.getFirstHeader(HttpHeaders.CONTENT_ENCODING);
        if (encodingHeader != null) {
            if (HttpClientConfig.CompressFormat.COMPRESS_FORMAT_GZIP.isBelong(encodingHeader.getValue())) {
                return HttpClientUtils.uncompressStreamRaw(HttpClientConfig.CompressFormat.COMPRESS_FORMAT_GZIP, entity.getContent());
            } else if (HttpClientConfig.CompressFormat.COMPRESS_FORMAT_DEFLATE.isBelong(encodingHeader.getValue())) {
                return HttpClientUtils.uncompressStreamRaw(HttpClientConfig.CompressFormat.COMPRESS_FORMAT_DEFLATE, entity.getContent());
            } else if (HttpClientConfig.CompressFormat.COMPRESS_FORMAT_IDENTITY.isBelong(encodingHeader.getValue())) {
                return EntityUtils.toByteArray(entity);
            } else {
                LOGGER.error("Unsupported HTTP compressed encoding:{}", encodingHeader.getValue());
                throw new RuntimeException("Unsupported HTTP compressed encoding:" + encodingHeader.getValue());
            }
        }
        // 如果response头部没有指示编码格式，认为非压缩，直接返回
        return EntityUtils.toByteArray(entity);
    }


}

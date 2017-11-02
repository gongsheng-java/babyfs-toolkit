package com.babyfs.tk.http.client;

import com.google.common.io.Closeables;
import com.google.common.net.MediaType;
import com.babyfs.tk.commons.Constants;
import com.babyfs.tk.http.constants.HttpClientConfig;
import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.DeflaterInputStream;
import java.util.zip.GZIPInputStream;

/**
 * Http客户端工具类
 * <p/>
 */
public final class HttpClientUtils {

    private HttpClientUtils() {
    }


    /**
     * 解压缩GZIP或者DEFLATE输入流
     *
     * @param compressFormat
     * @param inputStream    原始字节流
     * @param charset        编码
     * @return 解压缩后的字符串
     * @throws IOException
     */
    public static String uncompressStream(HttpClientConfig.CompressFormat compressFormat, InputStream inputStream, String charset) throws IOException {
        if (charset == null) {
            charset = Constants.UTF_8;
        }

        FilterInputStream compressStream = null;
        InputStreamReader inputReader = null;
        StringWriter strWriter = null;
        try {
            compressStream = getCompressStream(compressFormat, inputStream);
            inputReader = new InputStreamReader(compressStream, charset);
            strWriter = new StringWriter();
            IOUtils.copy(inputReader, strWriter);
            return strWriter.toString();
        } finally {
            //关闭顺序 :先外后里
            Closeables.close(strWriter, true);
            Closeables.closeQuietly(inputReader);
            Closeables.closeQuietly(compressStream);
        }
    }


    /**
     * 解压缩GZIP或者DEFLATE输入流
     *
     * @param compressFormat
     * @param inputStream    原始字节流
     * @return 解压缩后的字符串
     * @throws IOException
     */
    public static byte[] uncompressStreamRaw(HttpClientConfig.CompressFormat compressFormat, InputStream inputStream) throws IOException {
        FilterInputStream compressStream = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
        try {
            compressStream = getCompressStream(compressFormat, inputStream);
            IOUtils.copy(compressStream, out);
            return out.toByteArray();
        } finally {
            //关闭顺序 :先外后里
            Closeables.close(out, true);
            Closeables.closeQuietly(compressStream);
        }
    }

    /**
     * 组装拼接get请求参数
     *
     * @param getParams 请求的query参数，可以为null或长度为0
     * @return
     */
    public static URI assemblyGetURI(String url, Map<String, String> getParams) throws URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder(new URI(url));
        if (getParams != null) {
            for (Map.Entry<String, String> entry : getParams.entrySet()) {
                uriBuilder.addParameter(entry.getKey(), entry.getValue());
            }
        }
        return uriBuilder.build();
    }

    /**
     * 组装拼接post请求参数
     *
     * @param postMethod post方法
     * @param postParams post参数，可以为null或长度为0
     */
    public static void assemblyPostParams(HttpPost postMethod, Map<String, String> postParams) {
        if (postParams == null || postParams.size() == 0) {
            return;
        }

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        for (Map.Entry<String, String> entry : postParams.entrySet()) {
            nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        try {
            postMethod.setEntity(new UrlEncodedFormEntity(nvps, Constants.UTF_8));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 组装拼接请求头部
     *
     * @param httpMethod GET或POST等Http方法
     * @param headerMap  Http请求头部参数，可以为null或长度为0
     */
    public static void assemblyHeaders(HttpRequestBase httpMethod, Map<String, String> headerMap) {
        if (headerMap == null || headerMap.size() == 0) {
            return;
        }
        for (Map.Entry<String, String> entry : headerMap.entrySet()) {
            httpMethod.addHeader(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 组装拼接请求头部
     *
     * @param httpRequestBase GET或POST等Http方法
     * @param headerMap       Http请求头部参数，可以为null或长度为0
     */
    public static void assemblyAsyncHeaders(HttpRequestBase httpRequestBase, Map<String, String> headerMap) {
        if (headerMap == null || headerMap.size() == 0) {
            return;
        }
        for (Map.Entry<String, String> entry : headerMap.entrySet()) {
            httpRequestBase.addHeader(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 解析媒体类型
     *
     * @param contentType
     * @return 如果解析失败，返回null
     */
    public static MediaType parseMediaType(String contentType) {
        try {
            return MediaType.parse(contentType);
        } catch (Exception e) {
            //ignore it
        }
        return null;
    }

    public static FilterInputStream getCompressStream(HttpClientConfig.CompressFormat compressFormat, InputStream inputStream) throws IOException {
        switch (compressFormat) {
            case COMPRESS_FORMAT_GZIP:
                return new GZIPInputStream(inputStream);
            case COMPRESS_FORMAT_DEFLATE:
                return new DeflaterInputStream(inputStream);
            default:
                throw new IllegalArgumentException();
        }
    }

}

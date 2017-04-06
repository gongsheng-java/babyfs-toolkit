package com.babyfs.tk.http.constants;

/**
 * HTTP client的配置项
 * <p/>
 */
public final class HttpClientConfig {
    private HttpClientConfig() {

    }

    /** Http client 最大连接数 */
    public static final String CONF_HTTP_CLIENT_MAX_CONNECTION = "http.client.max.connection.num";

    /** Http client 连接超时 */
    public static final String CONF_HTTP_CLIENT_CONNECTION_TIMEOUT = "http.client.connection.timeout";

    /** Http client socket超时 */
    public static final String CONF_HTTP_CLIENT_SOCKET_TIMEOUT = "http.client.socket.timeout";


    /** 压缩格式枚举 */
    public enum CompressFormat {

        /** gzip压缩格式 */
        COMPRESS_FORMAT_GZIP("gzip", "x-gzip"),
        /* deflate压缩格式 */
        COMPRESS_FORMAT_DEFLATE("deflate", "x-deflate"),
        /* identity非压缩 */
        COMPRESS_FORMAT_IDENTITY("identity");

        /** 压缩格式字符串 */
        private final String[] encodings;

        CompressFormat(String... encodings) {
            this.encodings = encodings;
        }

        /**
         * 判断指定的编码格式是否被该枚举支持
         *
         * @param otherEncoding 需要判断的编码格式
         * @return 如果支持该编码返回true，否则返回false
         */
        public boolean isBelong(String otherEncoding) {
            for (String supported : encodings) {
                if (supported.equalsIgnoreCase(otherEncoding)) {
                    return true;
                }
            }
            return false;
        }

    }


}

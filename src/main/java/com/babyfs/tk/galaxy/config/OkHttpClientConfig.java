package com.babyfs.tk.galaxy.config;

public final class OkHttpClientConfig {

    private OkHttpClientConfig() {
    }

    //连接超时时间 单位秒
    public static final String CONNECT_TIMEOUT = "ok.http.connect.timeout";
    //读超时时间 单位秒
    public static final String READ_TIMEOUT = "ok.http.read.timeout";
    //写超时时间 单位秒
    public static final String WRITE_TIMEOUT = "ok.http.write.timeout";

}

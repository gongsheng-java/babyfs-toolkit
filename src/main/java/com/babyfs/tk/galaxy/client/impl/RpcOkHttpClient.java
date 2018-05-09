package com.babyfs.tk.galaxy.client.impl;

import com.babyfs.tk.galaxy.RpcException;
import com.babyfs.tk.galaxy.client.IClient;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

/**
 * 用OKHttpclient实现的http调用client
 * 枚举实现单例模式
 */
public class RpcOkHttpClient implements IClient {
    public static final MediaType BINARY = MediaType.parse("application/octet-stream; charset=utf-8");

    private OkHttpClient client;

    /**
     * 初始化OkHttpClient
     *
     * @param connectTimeOut
     * @param readTimeOut
     * @param writeTimeOut
     */
    public void init(long connectTimeOut, long readTimeOut, long writeTimeOut) {
        ConnectionPool connectionPool = new ConnectionPool();
        client = new OkHttpClient.Builder().connectionPool(connectionPool)
                .connectTimeout(connectTimeOut, TimeUnit.SECONDS)
                .readTimeout(readTimeOut, TimeUnit.SECONDS)
                .writeTimeout(writeTimeOut, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public byte[] execute(final String uri, final byte[] requestBody) throws IOException {
        Request request = new Request.Builder()
                .url(uri)
                .post(RequestBody.create(BINARY, requestBody))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.code() != 200) {
                throw new RpcException(format("error message(%s) ", response.message()));
            }

            ResponseBody responseBody = response.body();
            if (responseBody != null) {
                return responseBody.bytes();
            }
            return null;
        }
    }
}
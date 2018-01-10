package com.babyfs.tk.galaxy.client;

import com.babyfs.tk.galaxy.RpcException;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

/**
 * 用OKHttpclient实现的http调用client
 * 枚举实现单例模式
 */
public class RpcOkHttpClient implements IClient {


    public static final MediaType BINARY
            = MediaType.parse("application/octet-stream; charset=utf-8");

    private OkHttpClient client;


    public void init(long connectTimeOut, long readTimeOut, long writeTimeOut) {

        ConnectionPool connectionPool = new ConnectionPool();
        client = new OkHttpClient.Builder().connectionPool(connectionPool)
                .connectTimeout(connectTimeOut, TimeUnit.SECONDS)
                .readTimeout(readTimeOut, TimeUnit.SECONDS)
                .writeTimeout(writeTimeOut, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public byte[] execute(String uri, byte[] body) throws IOException {

        Request request = new Request.Builder()
                .url(uri)
                .post(RequestBody.create(BINARY, body))
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.code() != 200) {
                throw new RpcException(format("error message(%s) ", response.message()));
            }
            return response.body().bytes();
        }
    }
}

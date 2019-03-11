package com.babyfs.tk.galaxy.client.impl;

import com.babyfs.servicetk.grpcapicore.gray.GrayContext;
import com.babyfs.tk.galaxy.RpcException;
import com.babyfs.tk.galaxy.client.IClient;
import com.google.common.base.Strings;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

/**
 * 用OKHttpclient实现的http调用client
 * 枚举实现单例模式
 */
public class RpcOkHttpClient implements IClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcOkHttpClient.class);
    public static final MediaType BINARY = MediaType.parse("application/octet-stream; charset=utf-8");
    private static final String KEY_GRAY_FLAG = "gray";
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
        Request.Builder post = new Request.Builder()
                .url(uri)
                .post(RequestBody.create(BINARY, requestBody));

        if(GrayContext.get().isHasTag()){
            post.addHeader(KEY_GRAY_FLAG, String.join(",", GrayContext.get().getTags()));
        }

        Request request = post
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.code() != 200) {
                LOGGER.info("request = {},response = {} ",request.toString(),response.toString());
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

package com.babyfs.tk.galaxy.client;

import com.babyfs.tk.galaxy.RpcException;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

public enum RpcOkHttpClient implements Client{


    http;

    private static final  long CONNECT_TIMEOUT = 5;

    private static final  long READ_TIMEOUT = 5;

    private static final  long WRITE_TIMEOUT = 5;

    public static final MediaType BINARY
            = MediaType.parse("application/octet-stream; charset=utf-8");

    private OkHttpClient client ;

    private RpcOkHttpClient() {
        init();
    }

    private void  init(){

        ConnectionPool  connectionPool = new ConnectionPool();
        client = new OkHttpClient.Builder().connectionPool(connectionPool)
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT,TimeUnit.SECONDS)
                .build();
    }

    @Override
    public byte[] execute(String uri, byte[] body) throws IOException {

        Request request = new Request.Builder()
                .url(uri)
                .post(RequestBody.create(BINARY,body))
                .build();
        try (Response response = client.newCall(request).execute()) {
            if(response.code()!=200){
                throw new RpcException(format("error status(%s) ", response.code()));
            }
            return response.body().bytes();
        }
    }
}

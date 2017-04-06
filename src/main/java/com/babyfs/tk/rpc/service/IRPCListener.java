package com.babyfs.tk.rpc.service;

import com.babyfs.tk.rpc.Request;
import com.babyfs.tk.rpc.Response;

/**
 * RPC调用事件监听接口
 */
public interface IRPCListener {
    /**
     * 当有响应到达时
     *
     * @param response
     */
    public void onResponseReceived(Response response);

    /**
     * 当有请求到达时
     *
     * @param request
     */
    public void onRequestReceived(Request request);
}

package com.babyfs.tk.rpc.service.internal;

import com.babyfs.tk.commons.concurrent.ICallback;
import com.babyfs.tk.rpc.Response;

/**
 * RPC调用结果接受器
 */
public abstract class ResponseReceiver implements ICallback<Object, Response> {
    private volatile Response response;
    private final String serviceName;
    private final String methodName;
    private final String methodId;

    public ResponseReceiver(String serviceName, String methodName, String methodId) {
        this.serviceName = serviceName;
        this.methodName = methodName;
        this.methodId = methodId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getMethodId() {
        return methodId;
    }

    public synchronized Response getResponse() {
        return response;
    }

    public synchronized void setResponse(Response response) {
        this.response = response;
    }
}

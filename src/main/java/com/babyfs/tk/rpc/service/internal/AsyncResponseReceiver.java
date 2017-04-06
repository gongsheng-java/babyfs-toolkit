package com.babyfs.tk.rpc.service.internal;

import com.babyfs.tk.commons.concurrent.ICallback;
import com.babyfs.tk.rpc.Response;
import com.babyfs.tk.rpc.service.ServiceException;

/**
 */
public class AsyncResponseReceiver extends ResponseReceiver {
    private final ICallback callback;

    public AsyncResponseReceiver(String serviceName, String methodName, String methodId, ICallback callback) {
        super(serviceName, methodName, methodId);
        this.callback = callback;
    }

    @Override
    public synchronized void onFinish(Object aVoid, Response out) {
        this.setResponse(out);
        if (out.isSuccess()) {
            callback.onFinish(null, out.getResponse());
        } else {
            callback.onException(null, new ServiceException(out.getErrormsg()));
        }
    }

    @Override
    public void onException(Object aVoid, Exception e) {
        callback.onException(null, e);
    }
}

package com.babyfs.tk.rpc.service.internal;

import com.babyfs.tk.rpc.Response;

import java.util.concurrent.CountDownLatch;

/**
 * 同步调用Response接收器
 */
public final class SyncResponseReceiver extends ResponseReceiver {
    private final CountDownLatch countDownLatch;

    public SyncResponseReceiver(String serviceName, String methodName, String methodId) {
        super(serviceName, methodName, methodId);
        this.countDownLatch = new CountDownLatch(1);
    }

    @Override
    public synchronized void onFinish(Object aVoid, Response out) {
        this.setResponse(out);
        countDownLatch.countDown();
    }

    @Override
    public synchronized void onException(Object aVoid, Exception e) {
        this.setResponse(null);
        countDownLatch.countDown();
    }

    public CountDownLatch getCountDownLatch() {
        return countDownLatch;
    }
}

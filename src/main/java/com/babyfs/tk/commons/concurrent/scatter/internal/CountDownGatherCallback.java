package com.babyfs.tk.commons.concurrent.scatter.internal;

import com.babyfs.tk.commons.concurrent.scatter.IGather;
import com.babyfs.tk.commons.concurrent.scatter.IGatherCallback;

import java.util.concurrent.CountDownLatch;

/**
 * @param <SCATTER_OUT>
 * @param <GATHER_OUT>
 */
public class CountDownGatherCallback<SCATTER_OUT, GATHER_OUT> implements IGatherCallback<SCATTER_OUT, GATHER_OUT> {
    private final CountDownLatch countDownLatch;

    public CountDownGatherCallback() {
        this.countDownLatch = new CountDownLatch(1);
    }

    public CountDownLatch getCountDownLatch() {
        return countDownLatch;
    }

    @Override
    public void onFinish(IGather<SCATTER_OUT, GATHER_OUT> gather, Void out) {
        countDownLatch.countDown();
    }

    @Override
    public void onException(IGather<SCATTER_OUT, GATHER_OUT> gather, Exception e) {
        countDownLatch.countDown();
    }
}

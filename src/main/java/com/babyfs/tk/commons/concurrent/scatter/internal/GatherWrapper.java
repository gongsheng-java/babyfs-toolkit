package com.babyfs.tk.commons.concurrent.scatter.internal;

import com.babyfs.tk.commons.concurrent.scatter.IScatterTask;
import com.babyfs.tk.commons.concurrent.scatter.IGather;
import com.babyfs.tk.commons.concurrent.scatter.IGatherCallback;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link IGather}的封装
 *
 * @param <SCATTER_OUT>
 * @param <GATHER_OUT>
 */
public class GatherWrapper<SCATTER_OUT, GATHER_OUT> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GatherWrapper.class);
    private final List<FutureTask<?>> futures = Lists.newArrayList();
    private final IScatterTask<SCATTER_OUT> scatterTask;
    private final IGather<SCATTER_OUT, GATHER_OUT> gather;
    private final AtomicInteger count;
    private final IGatherCallback<SCATTER_OUT, GATHER_OUT> callack;
    private volatile boolean done = false;

    /**
     * @param scatterTask Scatter任务
     * @param gather      Gather
     * @param callback    回调接口
     */
    public GatherWrapper(IScatterTask<SCATTER_OUT> scatterTask, IGather<SCATTER_OUT, GATHER_OUT> gather, IGatherCallback<SCATTER_OUT, GATHER_OUT> callback) {
        Preconditions.checkArgument(!scatterTask.getScatters().isEmpty(), "scatterSize");
        Preconditions.checkNotNull(gather);
        Preconditions.checkNotNull(callback);
        this.scatterTask = scatterTask;
        this.gather = gather;
        this.count = new AtomicInteger(scatterTask.getScatters().size());
        this.callack = callback;
    }

    /**
     * 取得所有的FutureTask
     *
     * @return
     */
    public synchronized List<FutureTask<?>> getFutureTask() {
        return Lists.newArrayList(futures);
    }

    /**
     * 是否已经完成
     *
     * @return
     */
    public synchronized boolean isDone() {
        return done;
    }

    /**
     * @param task
     */
    public synchronized void addFutureTask(FutureTask<?> task) {
        Preconditions.checkNotNull(task);
        futures.add(task);
    }

    /**
     * 增加scatter的结果
     *
     * @param scatterOut
     */
    public synchronized void addScatterOut(SCATTER_OUT scatterOut) {
        if (done) {
            LOGGER.warn("The gather {} is done,ignore the scatter out {}", gather, scatterOut.getClass());
            return;
        }
        try {
            gather.append(scatterOut);
        } catch (Exception e) {
            onScatterException(e);
            return;
        }
        if (count.decrementAndGet() == 0) {
            complete(true);
            callack.onFinish(gather, null);
        }
    }


    /**
     * 当Scatter的发生异常时的回调接口
     *
     * @param e
     */
    public synchronized void onScatterException(Exception e) {
        LOGGER.error("Catched unexcepted exception,cancel the gather [" + gather + "]", e);
        if (done) {
            LOGGER.warn("The gather {} is done,ignore the scatter exception.", gather);
            return;
        }
        this.complete(false);
        callack.onException(gather, e);
    }

    /**
     * 因为超时取消任务
     */
    public synchronized void cancelForTimeout() {
        if (done) {
            return;
        }
        LOGGER.warn("Cancel the scatterTask:{}", this.scatterTask);
        this.complete(false);
        callack.onException(gather, new TimeoutException());
    }


    /**
     * 设置为完成状态
     *
     * @param isSuccess 是否成功:true,成功;false,失败
     */
    private synchronized void complete(boolean isSuccess) {
        LOGGER.debug("Complete scatterTask:{},success:{}", this.scatterTask, isSuccess);
        if (!isSuccess) {
            LOGGER.warn("Complete scatterTask:{},success:{},try to cancal all FutureTask.", this.scatterTask, isSuccess);
            for (FutureTask task : this.futures) {
                //取消所有的任务
                if (!task.isDone()) {
                    boolean result = task.cancel(true);
                    LOGGER.debug("Cancel futuerTask {},result {}", task, result);
                }
            }
        }
        this.futures.clear();
        this.done = true;
        this.scatterTask.setDone(true, isSuccess);
    }
}

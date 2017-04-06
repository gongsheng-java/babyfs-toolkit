package com.babyfs.tk.commons.concurrent.scatter;

import com.google.common.base.Preconditions;
import com.babyfs.tk.commons.concurrent.scatter.internal.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.*;

/**
 * Scatter + Gather 计算模式
 */
public class ScatterGather {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScatterGather.class);
    private final ExecutorService scatterExecutors;
    private final ScheduledExecutorService scheduledExecutorService;

    public ScatterGather() {
        scatterExecutors = Executors.newFixedThreadPool(2);
        scheduledExecutorService = Executors.newScheduledThreadPool(1);
    }

    /**
     * 在线程池{@link #scatterExecutors}中执行由<code>scatterTask</code>指定的任务.
     * <code>scatterTask</code>中的每个scatter作为计算任务的一部分,每个scatter的输出交给<code>gather</code>负责收集,处理,汇总.
     * 最后返回由<code>gather</code>处理后的结果.
     *
     * @param scatterTask   计算任务
     * @param gather        计算结果收集器
     * @param <SCATTER_OUT> <code>scatters</code>的输出结果类型
     * @param <GATHER_OUT>  <code>gather</code>的输出结构类型
     * @return 由<code>gather</code>处理后的最终结果
     * @throws IllegalArgumentException,RuntimeException
     */
    public <SCATTER_OUT, GATHER_OUT> GATHER_OUT compute(final IScatterTask<SCATTER_OUT> scatterTask, IGather<SCATTER_OUT, GATHER_OUT> gather) {
        CountDownGatherCallback<SCATTER_OUT, GATHER_OUT> countDownGatherCallback = new CountDownGatherCallback<SCATTER_OUT, GATHER_OUT>();
        computeOnExecutors(scatterTask, gather, countDownGatherCallback, 0);
        try {
            countDownGatherCallback.getCountDownLatch().await();
        } catch (InterruptedException e) {
            LOGGER.error("Cathch an InterruptedException.", e);
            //clean the interrupted state
            Thread.interrupted();
        }
        if (scatterTask.isDone() && scatterTask.isSuccess()) {
            return gather.get();
        } else {
            LOGGER.warn("The scatter task {} fails.", scatterTask);
            throw new RuntimeException("The scatter task fails.");
        }
    }

    /**
     * 在线程池{@link #scatterExecutors}中执行由<code>scatterTask</code>指定的任务.
     * <code>scatterTask</code>中的每个scatter作为计算任务的一部分,每个scatter的输出交给<code>gather</code>负责收集,处理,汇总.
     * 最后返回由<code>gather</code>处理后的结果.
     *
     * @param scatterTask   计算任务
     * @param gather        计算结果收集器
     * @param callack       结果的回调接口
     * @param timeout       超时时间,单位ms
     * @param <SCATTER_OUT>
     * @param <GATHER_OUT>
     * @throws IllegalArgumentException
     */
    public <SCATTER_OUT, GATHER_OUT> void computeWithCallback(final IScatterTask<SCATTER_OUT> scatterTask, final IGather<SCATTER_OUT, GATHER_OUT> gather, final IGatherCallback<SCATTER_OUT, GATHER_OUT> callack, long timeout) {
        computeOnExecutors(scatterTask, gather, callack, timeout);
    }

    /**
     * 在调用者的线程中触发异步任务的执行,即由<code>scatterTask</code>指定的任务都应该是异步任务.
     * <code>scatterTask</code>中的每个scatter作为计算任务的一部分,每个scatter的输出交给<code>gather</code>负责收集,处理,汇总.
     * 最后返回由<code>gather</code>处理后的结果.
     *
     * @param scatterTask   异步任务集合,并发/异步计算任务由该对象负责
     * @param gather        结果收集
     * @param <SCATTER_OUT> Scatter的输出结果的类型
     * @param <GATHER_OUT>  Gather的输出结果类型
     * @return
     */
    public <SCATTER_OUT, GATHER_OUT> GATHER_OUT computeForAsyncScatter(final IScatterTask<SCATTER_OUT> scatterTask, IGather<SCATTER_OUT, GATHER_OUT> gather) {
        Preconditions.checkArgument(scatterTask != null, "scatterTask");
        List<IScatter<SCATTER_OUT>> scatters = Preconditions.checkNotNull(scatterTask.getScatters());
        Preconditions.checkArgument(!scatters.isEmpty(), "scatterTask.scatters is empty");
        Preconditions.checkArgument(gather != null);
        CountDownGatherCallback<SCATTER_OUT, GATHER_OUT> countDownGatherCallback = new CountDownGatherCallback<SCATTER_OUT, GATHER_OUT>();
        final GatherWrapper<SCATTER_OUT, GATHER_OUT> gatherWrapper = new GatherWrapper<SCATTER_OUT, GATHER_OUT>(scatterTask, gather, countDownGatherCallback);
        for (final IScatter<SCATTER_OUT> scatter : scatters) {
            Preconditions.checkNotNull(scatter);
            ScatterRunnerInAsync<SCATTER_OUT> callable = new ScatterRunnerInAsync<SCATTER_OUT>(scatter, new AppendToGatherScatterCallback<SCATTER_OUT, GATHER_OUT>(gatherWrapper));
            try {
                callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        try {
            countDownGatherCallback.getCountDownLatch().await();
        } catch (InterruptedException e) {
            LOGGER.error("Cathch an InterruptedException.", e);
            //clean the interrupted state
            Thread.interrupted();
        }
        if (scatterTask.isDone() && scatterTask.isSuccess()) {
            return gather.get();
        } else {
            LOGGER.warn("The scatter task {} fails.", scatterTask);
            throw new RuntimeException("The scatter task fails.");
        }
    }

    /**
     * @param scatterTask
     * @param gather
     * @param callack
     * @param timeout
     * @param <SCATTER_OUT>
     * @param <GATHER_OUT>
     */
    private <SCATTER_OUT, GATHER_OUT> void computeOnExecutors(final IScatterTask<SCATTER_OUT> scatterTask, final IGather<SCATTER_OUT, GATHER_OUT> gather, final IGatherCallback<SCATTER_OUT, GATHER_OUT> callack, long timeout) {
        Preconditions.checkArgument(scatterTask != null, "scatterTask");
        List<IScatter<SCATTER_OUT>> scatters = Preconditions.checkNotNull(scatterTask.getScatters());
        Preconditions.checkArgument(!scatters.isEmpty(), "scatterTask.scatters is empty");
        Preconditions.checkArgument(gather != null);
        final GatcherCallbackWrapper<SCATTER_OUT, GATHER_OUT> callbackWrapper = new GatcherCallbackWrapper<SCATTER_OUT, GATHER_OUT>(callack);
        final GatherWrapper<SCATTER_OUT, GATHER_OUT> gatherWrapper = new GatherWrapper<SCATTER_OUT, GATHER_OUT>(scatterTask, gather, callbackWrapper);
        FutureTask<Void> timeoutTask = null;
        if (timeout > 0) {
            TimeoutRunnable timeoutRunnable = new TimeoutRunnable(gatherWrapper);
            timeoutTask = new FutureTask<Void>(timeoutRunnable, null);
            callbackWrapper.setTimeoutFutureTask(timeoutTask);
        }
        for (final IScatter<SCATTER_OUT> scatter : scatters) {
            Preconditions.checkNotNull(scatter);
            ScatterRunnerInSync<SCATTER_OUT> callable = new ScatterRunnerInSync<SCATTER_OUT>(scatter, new AppendToGatherScatterCallback<SCATTER_OUT, GATHER_OUT>(gatherWrapper));
            FutureTask task = new FutureTask(callable);
            gatherWrapper.addFutureTask(task);
        }
        for (FutureTask task : gatherWrapper.getFutureTask()) {
            scatterExecutors.submit(task);
        }
        if (timeout > 0) {
            //开始超时及时
            scheduledExecutorService.schedule(timeoutTask, timeout, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * @param <SCATTER_OUT>
     * @param <GATHER_OUT>
     */
    private static class GatcherCallbackWrapper<SCATTER_OUT, GATHER_OUT> implements IGatherCallback<SCATTER_OUT, GATHER_OUT> {
        private final IGatherCallback<SCATTER_OUT, GATHER_OUT> realCallback;
        private FutureTask timeoutFutureTask;

        private GatcherCallbackWrapper(IGatherCallback<SCATTER_OUT, GATHER_OUT> realCallback) {
            this.realCallback = realCallback;
        }

        public void setTimeoutFutureTask(FutureTask timeoutFutureTask) {
            this.timeoutFutureTask = timeoutFutureTask;
        }

        @Override
        public void onFinish(IGather<SCATTER_OUT, GATHER_OUT> gather, Void out) {
            if (this.timeoutFutureTask != null) {
                this.timeoutFutureTask.cancel(true);
            }
            this.realCallback.onFinish(gather, out);
        }

        @Override
        public void onException(IGather<SCATTER_OUT, GATHER_OUT> gather, Exception e) {
            if (this.timeoutFutureTask != null) {
                this.timeoutFutureTask.cancel(true);
            }
            this.realCallback.onException(gather, e);
        }
    }

    /**
     * 调用的超时Runnable
     *
     * @param <SCATTER_OUT>
     * @param <GATHER_OUT>
     */
    private static class TimeoutRunnable<SCATTER_OUT, GATHER_OUT> implements Runnable {
        private final GatherWrapper<SCATTER_OUT, GATHER_OUT> gatherWrapper;

        public TimeoutRunnable(GatherWrapper<SCATTER_OUT, GATHER_OUT> gatherWrapper) {
            this.gatherWrapper = gatherWrapper;
        }

        @Override
        public void run() {
            if (!gatherWrapper.isDone()) {
                //如果到时间了还没有完成,则取消任务
                gatherWrapper.cancelForTimeout();
            }
        }
    }
}

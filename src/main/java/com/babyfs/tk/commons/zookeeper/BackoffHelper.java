package com.babyfs.tk.commons.zookeeper;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 执行补偿任务的帮助类,所谓补偿是指当某一项任务或者操作失败后,可以采取一定的补救措施。
 * 本类提供了两种补偿方式：
 * {@link #doUntilSuccess(com.google.common.base.Function, long)} 同步地持续地调用指定的Function,直到成功为止
 * {@link #doUntilSuccessAtExecutor}} 异步地持续地调用指定的Function,直到成功为止
 */
public class BackoffHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(BackoffHelper.class);
    private static final ScheduledExecutorService DEFAULT_EXECUTORS = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledExecutorService executorService;

    public BackoffHelper() {
        this(DEFAULT_EXECUTORS);
    }

    public BackoffHelper(ScheduledExecutorService executorService) {
        this.executorService = Preconditions.checkNotNull(executorService);
    }

    /**
     * 执行指定的Function,直到成功为止.
     * 需要特别注意的是,如果<code>function</code>一直不成功,那么就有可能进入死循环
     *
     * @param function
     */
    public boolean doUntilSuccess(Function<Void, Boolean> function, long maxWaitMS) {
        BackoffTask task = new BackoffTask(function);
        long wait = 0;
        while (!task.isSuccess()) {
            try {
                task.run();
            } catch (Exception e) {
                LOGGER.error("Exec function [" + function + "] fail,continue try it.", e);
            }
            if (!task.isSuccess()) {
                try {
                    if (maxWaitMS > 0 && wait >= maxWaitMS) {
                        throw new RuntimeException("max wait " + maxWaitMS);
                    }
                    long nextWaitTime = task.calcNextWaitTime();
                    Thread.sleep(nextWaitTime);
                    wait += nextWaitTime;
                } catch (InterruptedException e) {
                    LOGGER.error("Interruptted by other thread.", e);
                    Thread.currentThread().interrupt();
                }
            } else {
                break;
            }
        }
        return task.isSuccess();
    }

    /**
     * 在Executors中执行指定的Function
     * 需要特别注意的是,如果<code>function</code>一直不成功,那么就会一直地执行这个function,直到成功为止.
     *
     * @param function
     */
    public void doUntilSuccessAtExecutor(final Function<Void, Boolean> function) {
        final BackoffTask task = new BackoffTask(function);
        Runnable runnable = new BackoffTaskRunnable(task);
        executorService.schedule(runnable, 0, TimeUnit.MILLISECONDS);
    }

    /**
     * 在Executors中重复执行指定的Function
     *
     * @param function       被定期重复执行的函数,function的返回值为true,表示继续执行;false,表示停止执行
     * @param delayMillis    延迟时间,单位毫秒
     * @param intervalMillis 间隔,单位毫秒
     */
    public void doRepeatTask(final Function<Void, Boolean> function, long delayMillis, long intervalMillis) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Boolean repeat = function.apply(null);
                    if (repeat == Boolean.TRUE) {
                        executorService.schedule(this, delayMillis, TimeUnit.MILLISECONDS);
                    }
                } catch (Exception e) {
                    LOGGER.error("running repeat function at executor fail", e);
                }
            }
        };
        executorService.schedule(runnable, delayMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * 用于补偿的任务
     */
    private static final class BackoffTask implements Runnable {
        public static final int MAX_WAIT_TIME = 10 * 1000;
        private final Function<Void, Boolean> function;
        private volatile boolean success = false;
        private final AtomicBoolean running = new AtomicBoolean(false);
        /**
         * 下一次执行前的等待时间
         */
        private long waitTime = 50;

        private BackoffTask(Function<Void, Boolean> function) {
            this.function = Preconditions.checkNotNull(function);
        }

        @Override
        public void run() {
            if (!running.compareAndSet(false, true)) {
                return;
            }
            try {
                if (function.apply(null)) {
                    success = true;
                }
            } finally {
                running.compareAndSet(true, false);
            }
        }

        public boolean isSuccess() {
            return success;
        }

        /**
         * @return
         */
        public long calcNextWaitTime() {
            long nextTime = Math.min(MAX_WAIT_TIME, waitTime * 2);
            waitTime = nextTime;
            return nextTime;
        }

        public Function<Void, Boolean> getFunction() {
            return function;
        }
    }

    /**
     *
     */
    private class BackoffTaskRunnable implements Runnable {
        private final BackoffTask task;

        public BackoffTaskRunnable(BackoffTask task) {
            this.task = task;
        }

        @Override
        public void run() {
            try {
                task.run();
            } catch (Exception e) {
                LOGGER.error("Running function at executor fail", e);
            }
            if (!task.isSuccess()) {
                long nextTime = task.calcNextWaitTime();
                LOGGER.info("Reschedule the function {} at executor within {} ms .", task.getFunction(), nextTime);
                executorService.schedule(this, nextTime, TimeUnit.MILLISECONDS);
            } else {
                LOGGER.info("Function {} exec succsss.", task.getFunction());
            }
        }
    }
}

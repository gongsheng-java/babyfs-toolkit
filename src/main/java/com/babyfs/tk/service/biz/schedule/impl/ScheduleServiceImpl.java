package com.babyfs.tk.service.biz.schedule.impl;

import com.babyfs.tk.service.biz.schedule.IScheduleService;
import com.google.common.base.Preconditions;
import com.babyfs.tk.commons.service.LifeServiceSupport;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 定时调度服务实现 ：
 * <p/>
 * 负责调度所有的定时，延时任务
 * <p/>
 */
public class ScheduleServiceImpl extends LifeServiceSupport implements IScheduleService {

    /**
     * 调度器
     */
    private ScheduledExecutorService scheduler;

    /**
     * 初始化调度服务
     *
     * @param poolSize 初始化线程池个数
     */
    public ScheduleServiceImpl(int poolSize) {
        Preconditions.checkArgument(poolSize > 0, "The poolSize must larger than zero.");
        this.scheduler = Executors.newScheduledThreadPool(poolSize);
    }

    /**
     * 关闭调度器
     *
     * @param isSafeShut 是否安全关闭，已有任务将执行完成
     */
    @Override
    public void shutDown(boolean isSafeShut) {
        if (this.scheduler != null && !this.scheduler.isShutdown()) {
            if (isSafeShut) {
                this.scheduler.shutdown();
            } else {
                this.scheduler.shutdownNow();
            }
        }
    }

    /**
     * 增加一个一次性延迟任务
     *
     * @param task  任务
     * @param delay 延时时间(毫秒)
     */
    @Override
    public ScheduledFuture<?> addOnceTimeTask(Runnable task, long delay) {
        Preconditions.checkArgument(task != null, "Cant't add null task.");
        return this.scheduler.schedule(task, delay, TimeUnit.MILLISECONDS);
    }

    /**
     * 创建一个持续延时定时任务,每次执行完成后经过指定间隔时间后再次执行
     *
     * @param task         任务
     * @param initialDelay 第一次执行的延时时间
     * @param delay        每次执行的延时
     * @return
     */
    @Override
    public ScheduledFuture<?> addFixedDelayTask(Runnable task, long initialDelay, long delay) {
        Preconditions.checkArgument(task != null, "Cant't add null task.");
        Preconditions.checkArgument(delay > 0, "The delay must larger than zero.");
        return this.scheduler.scheduleWithFixedDelay(task, initialDelay, delay, TimeUnit.MILLISECONDS);
    }

    /**
     * 创建一个指定停止时间的持续延时定时任务
     *
     * @param task         任务
     * @param initialDelay 第一次执行的延时时间
     * @param delay        每次执行的延时
     * @param cancelDelay  任务取消的延时
     * @return
     */
    @Override
    public ScheduledFuture<?> addFixedDelayTaskWithCancel(Runnable task, long initialDelay, long delay, long cancelDelay) {
        Preconditions.checkArgument(task != null, "Cant't add null task.");
        Preconditions.checkArgument(delay > 0, "The delay must larger than zero.");
        Preconditions.checkArgument(cancelDelay > delay, "The cancelDelay must larger than delay.");
        final ScheduledFuture<?> taskHandler = this.addFixedDelayTask(task, initialDelay, delay);
        if (cancelDelay > 0) {
            this.scheduler.schedule(new Runnable() {
                @Override
                public void run() {
                    taskHandler.cancel(true);
                }
            }, cancelDelay, TimeUnit.MILLISECONDS);
        }
        return taskHandler;
    }

    /**
     * 创建一个持续间隔定时任务,每隔指定的间隔时间后将执行一次（无论上一次任务是否完成）
     *
     * @param task         任务
     * @param initialDelay 第一次执行的延时时间
     * @param period        每次执行的间隔
     * @return
     */
    @Override
    public ScheduledFuture<?> addFixedRateTask(Runnable task, long initialDelay, long period) {
        Preconditions.checkArgument(task != null, "Cant't add null task.");
        Preconditions.checkArgument(period > 0, "The period must larger than zero.");
        return this.scheduler.scheduleAtFixedRate(task, initialDelay, period, TimeUnit.MILLISECONDS);
    }

    /**
     * 创建一个指定停止时间的持续间隔定时任务
     *
     * @param task         任务
     * @param initialDelay 第一次执行的延时时间
     * @param period        每次执行的间隔
     * @param cancelDelay  任务取消的延时
     * @return
     */
    @Override
    public ScheduledFuture<?> addFixedRateTaskWithCancel(Runnable task, long initialDelay, long period, long cancelDelay) {
        Preconditions.checkArgument(task != null, "Cant't add null task.");
        Preconditions.checkArgument(period > 0, "The period must larger than zero.");
        Preconditions.checkArgument(cancelDelay > period, "The cancelDelay must larger than period.");
        final ScheduledFuture<?> taskHandler = this.addFixedRateTask(task, initialDelay, period);
        if (cancelDelay > 0) {
            this.scheduler.schedule(new Runnable() {
                @Override
                public void run() {
                    taskHandler.cancel(true);
                }
            }, cancelDelay, TimeUnit.MILLISECONDS);
        }
        return taskHandler;
    }

    /**
     * 取消一个定时任务
     *
     * @param taskHandler
     * @param cancelDelay
     * @return
     */
    @Override
    public ScheduledFuture<?> cancelOneTask(final ScheduledFuture<?> taskHandler, long cancelDelay) {
        Preconditions.checkArgument(taskHandler != null, "Cant't cancel null task.");
        Preconditions.checkArgument(cancelDelay >= 0, "The cancelDelay must not little than zero.");
        return this.scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                taskHandler.cancel(true);
            }
        }, cancelDelay, TimeUnit.MILLISECONDS);
    }

}

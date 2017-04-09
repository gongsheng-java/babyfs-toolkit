package com.babyfs.tk.service.biz.schedule;


import com.babyfs.tk.commons.service.ILifeService;

import java.util.concurrent.ScheduledFuture;

/**
 * 调度服务接口
 * <p/>
 */
public interface IScheduleService extends ILifeService {

    /**
     * 关闭调度器
     *
     * @param isSafeShut 是否安全关闭，已有任务将执行完成
     */
    public void shutDown(boolean isSafeShut);

    /**
     * 增加一个一次性延迟任务
     *
     * @param task  任务
     * @param delay 延时时间(毫秒)
     */
    public ScheduledFuture<?> addOnceTimeTask(Runnable task, long delay);

    /**
     * 创建一个持续延时任务，在执行完第一次后将延时一定时间执行第二次
     *
     * @param task         任务
     * @param initialDelay 第一次执行的延时时间
     * @param delay        每次执行的间隔
     * @return
     */
    public ScheduledFuture<?> addFixedDelayTask(Runnable task, long initialDelay, long delay);

    /**
     * 创建一个含取消时间的持续延时任务
     *
     * @param task         任务
     * @param initialDelay 第一次执行的延时时间
     * @param delay        每次执行的间隔
     * @param cancelDelay  任务取消的延时
     * @return
     */
    public ScheduledFuture<?> addFixedDelayTaskWithCancel(Runnable task, long initialDelay, long delay, long cancelDelay);

    /**
     * 创建一个持续间隔任务,每隔指定的间隔时间后将执行一次（无论上一次任务是否完成）
     *
     * @param task         任务
     * @param initialDelay 第一次执行的延时时间
     * @param period       每次执行的间隔
     * @return
     */
    public ScheduledFuture<?> addFixedRateTask(Runnable task, long initialDelay, long period);

    /**
     * 创建一个指定停止时间的持续间隔任务
     *
     * @param task         任务
     * @param initialDelay 第一次执行的延时时间
     * @param period       每次执行的间隔
     * @param cancelDelay  任务取消的延时
     * @return
     */
    public ScheduledFuture<?> addFixedRateTaskWithCancel(Runnable task, long initialDelay, long period, long cancelDelay);

    /**
     * 取消一个定时任务
     *
     * @param taskHandler
     * @param cancelDelay
     * @return
     */
    public ScheduledFuture<?> cancelOneTask(final ScheduledFuture<?> taskHandler, long cancelDelay);

}

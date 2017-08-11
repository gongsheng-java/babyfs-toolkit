package com.babyfs.tk.commons.service;

/**
 * 提供定时执行任务的服务
 */
public interface IPeriodTaskServcie extends ILifeService {
    /**
     * 提交定时执行的任务
     *
     * @param periodMillis 定时任务的执行间隔,单位毫秒,>0,
     * @param task         需执行的任务,not null
     * @param stopCommand  用于停止task的命令,可以为空
     */
    void scheduleTask(long periodMillis, Runnable task, Runnable stopCommand);
}

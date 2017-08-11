package com.babyfs.tk.commons.service.internal;

import com.babyfs.tk.commons.base.Pair;
import com.babyfs.tk.commons.base.Tuple;
import com.babyfs.tk.commons.service.IPeriodTaskServcie;
import com.babyfs.tk.commons.service.LifeServiceSupport;
import com.babyfs.tk.commons.utils.ThreadUtil;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 按指定时间间隔运行的任务
 */
public class PeriodTaskServcie extends LifeServiceSupport implements IPeriodTaskServcie {
    private static final Logger LOGGER = LoggerFactory.getLogger(PeriodTaskServcie.class);
    private final ScheduledExecutorService executorService;
    private LinkedHashSet<Pair<ScheduledFuture, Runnable>> stopCommands = Sets.newLinkedHashSet();
    private List<Tuple<Long, Runnable, Runnable>> tasks = Lists.newArrayList();

    public PeriodTaskServcie() {
        executorService = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() / 2 + 1);
    }

    @Override
    public synchronized void scheduleTask(long periodMillis, Runnable task, Runnable stopCommand) {
        Preconditions.checkArgument(periodMillis > 0, "intervalMillis must >0");
        Preconditions.checkNotNull(task);
        tasks.add(Tuple.of(periodMillis, task, stopCommand));
    }

    @Override
    protected synchronized void execStart() {
        super.execStart();
        for (Tuple<Long, Runnable, Runnable> taskTuple : this.tasks) {
            long periodMillis = taskTuple.getFirst();
            Runnable task = taskTuple.getSecond();
            Runnable stopCommand = taskTuple.getThird();
            LOGGER.info("schedule task {} in {} mills", task.getClass(), periodMillis);
            ScheduledFuture<?> scheduledFuture = executorService.scheduleAtFixedRate(task, 0, periodMillis, TimeUnit.MILLISECONDS);
            stopCommands.add(Pair.of(scheduledFuture, stopCommand));
        }
    }

    @Override
    protected synchronized void execStop() {
        LOGGER.info("stop" + this.getClass());
        super.execStop();

        for (Pair<ScheduledFuture, Runnable> pair : stopCommands) {
            ScheduledFuture future = pair.getFirst();
            Runnable stopCommand = pair.getSecond();
            if (stopCommand != null) {
                stopCommand.run();
            }
            future.cancel(false);
        }

        LOGGER.info("shutdown");
        ThreadUtil.shutdownAndAwaitTermination(executorService, 30);
        LOGGER.info("shutdown finish");
    }
}

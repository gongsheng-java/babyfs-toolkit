package com.babyfs.tk.service.biz.counter.impl;

import com.babyfs.tk.commons.service.LifeServiceSupport;
import com.babyfs.tk.commons.utils.ThreadUtil;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link RedisCounterSyncService}定时调度
 */
public class RedisCounterSyncSchedule extends LifeServiceSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisCounterSyncSchedule.class);
    private List<RedisCounterSyncService> syncServcies;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final int scanIntervalMillis;
    private volatile boolean stop = false;
    private final Lock lock = new ReentrantLock();
    private final Condition sleepCondtion = lock.newCondition();

    /**
     * @param syncServices
     * @param scanIntervalSecond
     */
    public RedisCounterSyncSchedule(List<RedisCounterSyncService> syncServices, int scanIntervalSecond) {
        this.syncServcies = Preconditions.checkNotNull(syncServices);
        Preconditions.checkArgument(scanIntervalSecond > 0);
        this.scanIntervalMillis = scanIntervalSecond * 1000;
    }

    @Override
    protected void execStart() {
        super.execStart();
        executorService.submit(() -> {
            LOGGER.info("redis counter sync schedule started");
            while (!stop) {
                long st = System.currentTimeMillis();
                for (RedisCounterSyncService syncService : syncServcies) {
                    try {
                        LOGGER.info("beigin scan {}", syncService.getRedisCounterService().getName());
                        syncService.scanAll();
                        LOGGER.info("finish scan {}", syncService.getRedisCounterService().getName());
                    } catch (Exception e) {
                        LOGGER.error("scan fail", e);
                    }
                }
                LOGGER.info("finish redis counter sync ");
                long sleepMillis = this.scanIntervalMillis - (System.currentTimeMillis() - st);
                if (sleepMillis <= 0) {
                    sleepMillis = 5000;
                }

                lock.lock();
                try {
                    LOGGER.info("sleep {} ms", sleepMillis);
                    sleepCondtion.await(sleepMillis, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    ThreadUtil.onInterruptedException(e);
                    break;
                } finally {
                    lock.unlock();
                }
            }
            LOGGER.info("redis counter sync schedule stopped");
        });
    }

    @Override
    protected void execStop() {
        super.execStop();
        this.stop = true;
        for (RedisCounterSyncService syncService : syncServcies) {
            syncService.setStop(true);
        }

        lock.lock();
        try {
            sleepCondtion.signalAll();
        } finally {
            lock.unlock();
        }

        LOGGER.info("shutdown shcudule");
        ThreadUtil.shutdownAndAwaitTermination(executorService, 30);
        LOGGER.info("shutdown shcudule finish");
    }
}

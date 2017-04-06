package com.babyfs.tk.commons.utils;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 提供给线程使用的工具类
 */
public final class ThreadUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadUtil.class);

    private ThreadUtil() {

    }

    /**
     * 处理Thead产生的{@link InterruptedException},记录异常日志,重置当前线程的中断状态
     *
     * @param e
     * @see {@link Thread#interrupt()}
     */
    public static void onInterruptedException(InterruptedException e) {
        LOGGER.error("Catch an interruppted exception,reset the interrupted status", e);
        Thread.currentThread().interrupt();
    }

    /**
     * 停止executor
     *
     * @param executor    被停止的executor,不能为空
     * @param waitSeconds 等待executor停止的时间,单位秒
     */
    public static void shutdownAndAwaitTermination(ExecutorService executor, int waitSeconds) {
        Preconditions.checkNotNull(executor);
        Preconditions.checkArgument(waitSeconds > 0, "waitSeconds must >0");

        executor.shutdown();
        try {
            if (!executor.awaitTermination(waitSeconds, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(waitSeconds, TimeUnit.SECONDS)) {
                    LOGGER.error("executor did not terminate");
                }
            }
        } catch (InterruptedException e) {
            LOGGER.error("Shutdown executor interrupted", e);
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

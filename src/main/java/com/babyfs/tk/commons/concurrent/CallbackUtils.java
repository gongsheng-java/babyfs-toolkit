package com.babyfs.tk.commons.concurrent;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ICallback}的工具类
 */
public final class CallbackUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(CallbackUtils.class);

    private static final ThreadLocal<ICallback> CALLBACK_THREAD_LOCAL = new ThreadLocal<ICallback>();

    private CallbackUtils() {
    }

    /**
     * 在当前线程中设置一个{@link ICallback}对象
     *
     * @param callback 不能为null
     */
    public static void setUpCallbackOnCurThread(ICallback callback) {
        Preconditions.checkNotNull(callback, "callback");
        ICallback<?, ?> preCallback = CALLBACK_THREAD_LOCAL.get();
        if (preCallback != null) {
            LOGGER.warn("The previous callback object is {},maybe forget clean it?", preCallback);
        }
        CALLBACK_THREAD_LOCAL.set(callback);
    }

    /**
     * 取得当前线程中设置的{@link ICallback}对象
     *
     * @return
     */
    public static ICallback getUpCallbackOnCurThread() {
        return CALLBACK_THREAD_LOCAL.get();
    }

    /**
     * 删除当前线程中设置的{@link ICallback}对象
     */
    public static void cleanupCallbackOnCurThread() {
        ICallback preCallback = CALLBACK_THREAD_LOCAL.get();
        if (preCallback == null) {
            LOGGER.warn("The previous callback object is null,maybe reduplicately call cleanupCallbackOnCurThread?");
        }
        CALLBACK_THREAD_LOCAL.remove();
    }
}

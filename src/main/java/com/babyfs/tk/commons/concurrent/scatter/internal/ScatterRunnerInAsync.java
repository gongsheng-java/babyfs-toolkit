package com.babyfs.tk.commons.concurrent.scatter.internal;

import com.babyfs.tk.commons.concurrent.CallbackUtils;
import com.babyfs.tk.commons.concurrent.ICallback;
import com.babyfs.tk.commons.concurrent.scatter.IScatter;
import com.babyfs.tk.commons.concurrent.scatter.IScatterCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * 采用异步方式执行Scatter,采用这种模式执行Scatter时,不会等待{@link IScatter#call()}的执行完成,
 * 此时call方法的内部必须是异步执行的,即call方法能够尽快地返回.
 * <p/>
 * 而当call所调用的方法执行完成后,通过{@link CallbackUtils#getUpCallbackOnCurThread()}会得到回调接口对象,通知回调接口完成的结果.
 * <p/>
 *
 * @param <SCATTER_OUT>
 * @see {@link IScatter}
 * @see {@link CallbackUtils}
 */
public class ScatterRunnerInAsync<SCATTER_OUT> implements Callable<SCATTER_OUT> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScatterRunnerInAsync.class);
    private final IScatter<SCATTER_OUT> scatter;
    private final IScatterCallback<SCATTER_OUT> scatterCallback;
    private final ScatterAsyncCallback scatterAsyncCallback;

    public ScatterRunnerInAsync(IScatter<SCATTER_OUT> scatter, IScatterCallback scatterCallback) {
        this.scatter = scatter;
        this.scatterCallback = scatterCallback;
        this.scatterAsyncCallback = new ScatterAsyncCallback();
    }

    @Override
    public SCATTER_OUT call() throws Exception {
        CallbackUtils.setUpCallbackOnCurThread(this.scatterAsyncCallback);
        try {
            scatter.call();
            return null;
        } catch (Exception e) {
            LOGGER.error("Scatter [" + scatter + "] fail.", e);
            scatterCallback.onException(scatter, e);
            throw e;
        } finally {
            CallbackUtils.cleanupCallbackOnCurThread();
        }
    }

    private class ScatterAsyncCallback implements ICallback<Object, SCATTER_OUT> {
        @Override
        public void onFinish(Object o, SCATTER_OUT out) {
            scatterCallback.onFinish(scatter, out);
        }

        @Override
        public void onException(Object o, Exception e) {
            scatterCallback.onException(scatter, e);
        }
    }
}

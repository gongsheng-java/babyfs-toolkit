package com.babyfs.tk.commons.concurrent.scatter.internal;

import com.babyfs.tk.commons.concurrent.scatter.IScatter;
import com.babyfs.tk.commons.concurrent.scatter.IScatterCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * 采用同步方式执行的Scatter. 采用这种模式执行Scatter时,会等待{@link IScatter#call()}的执行完成,
 * 并将call的返回结果作为scall结果返回.
 *
 * {@link IScatter}的封装
 *
 * @param <SCATTER_OUT>
 */
public class ScatterRunnerInSync<SCATTER_OUT> implements Callable<SCATTER_OUT> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScatterRunnerInSync.class);
    private final IScatter<SCATTER_OUT> scatter;
    private final IScatterCallback<SCATTER_OUT> scatterCallback;

    public ScatterRunnerInSync(IScatter<SCATTER_OUT> scatter, IScatterCallback scatterCallback) {
        this.scatter = scatter;
        this.scatterCallback = scatterCallback;
    }

    @Override
    public SCATTER_OUT call() throws Exception {
        try {
            SCATTER_OUT out = scatter.call();
            scatterCallback.onFinish(scatter, out);
            return out;
        } catch (Exception e) {
            LOGGER.error("Scatter [" + scatter + "] fail.", e);
            scatterCallback.onException(scatter, e);
            throw e;
        }
    }
}

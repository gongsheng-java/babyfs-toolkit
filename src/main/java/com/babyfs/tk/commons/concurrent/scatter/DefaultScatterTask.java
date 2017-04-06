package com.babyfs.tk.commons.concurrent.scatter;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 *
 * @param <SCATTER_OUT> SCATTER的输出数据类型
 */
public class DefaultScatterTask<SCATTER_OUT> implements IScatterTask<SCATTER_OUT> {
    private final Object id;
    private boolean done;
    private boolean success;
    private ImmutableList<IScatter<SCATTER_OUT>> scatters;

    public DefaultScatterTask(Object id, ImmutableList<IScatter<SCATTER_OUT>> scatterList) {
        this.id = Preconditions.checkNotNull(id, "is must not be null");
        scatters = Preconditions.checkNotNull(scatterList, "scatterList must not be null");
    }

    @Override
    public Object getId() {
        return id;
    }

    @Override
    public ImmutableList<IScatter<SCATTER_OUT>> getScatters() {
        return scatters;
    }

    @Override
    public synchronized void setDone(boolean done, boolean success) {
        this.done = done;
        this.success = success;
    }

    @Override
    public synchronized boolean isDone() {
        return this.done;
    }

    @Override
    public synchronized boolean isSuccess() {
        return this.success;
    }
}

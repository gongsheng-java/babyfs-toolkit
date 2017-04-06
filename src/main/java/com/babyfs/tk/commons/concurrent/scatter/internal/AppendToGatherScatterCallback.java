package com.babyfs.tk.commons.concurrent.scatter.internal;

import com.babyfs.tk.commons.concurrent.scatter.IScatter;
import com.babyfs.tk.commons.concurrent.scatter.IScatterCallback;

/**
 * @param <SCATTER_OUT>
 * @param <GATHER_OUT>
 */
public class AppendToGatherScatterCallback<SCATTER_OUT, GATHER_OUT> implements IScatterCallback<SCATTER_OUT> {
    private final GatherWrapper<SCATTER_OUT, GATHER_OUT> gatherWrapper;

    public AppendToGatherScatterCallback(GatherWrapper<SCATTER_OUT, GATHER_OUT> gatherWrapper) {
        this.gatherWrapper = gatherWrapper;
    }

    @Override
    public void onFinish(IScatter<SCATTER_OUT> scatter, SCATTER_OUT out) {
        gatherWrapper.addScatterOut(out);
    }

    @Override
    public void onException(IScatter<SCATTER_OUT> scatter, Exception e) {
        gatherWrapper.onScatterException(e);
    }
}

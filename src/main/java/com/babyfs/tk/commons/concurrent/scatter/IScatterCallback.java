package com.babyfs.tk.commons.concurrent.scatter;

import com.babyfs.tk.commons.concurrent.ICallback;

/**
 * {@link IScatter}的回调接口
 *
 * @param <SCATTER_OUT>
 */
public interface IScatterCallback<SCATTER_OUT> extends ICallback<IScatter<SCATTER_OUT>,SCATTER_OUT> {

}

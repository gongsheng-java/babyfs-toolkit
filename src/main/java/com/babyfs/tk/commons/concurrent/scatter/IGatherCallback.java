package com.babyfs.tk.commons.concurrent.scatter;

import com.babyfs.tk.commons.concurrent.ICallback;

/**
 * {@link IGather}的回调接口
 *
 * @param <SCATTER_OUT> scatter的输出数据类型
 * @param <GATHER_OUT>  gather的输出数据类型
 */
public interface IGatherCallback<SCATTER_OUT, GATHER_OUT> extends ICallback<IGather<SCATTER_OUT, GATHER_OUT>, Void> {
}

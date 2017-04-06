package com.babyfs.tk.commons.concurrent.scatter;

import com.google.common.collect.ImmutableList;

/**
 * {@link IScatter}的任务接口
 *
 * @param <OUTPUT> {@link IScatter}的数据结果类型
 */
public interface IScatterTask<OUTPUT> {
    /**
     * 任务的id
     *
     * @return
     */
    Object getId();

    /**
     * 该任务的{@link IScatter}列表
     *
     * @return
     */
    ImmutableList<IScatter<OUTPUT>> getScatters();

    /**
     * 设置任务是否完成
     *
     * @param done    是否完成:true,完成;false,未完成
     * @param success 是否成功:true,成功;false,失败
     */
    void setDone(boolean done, boolean success);

    /**
     * 任务是否完成
     *
     * @return
     */
    boolean isDone();


    /**
     * 任务是否取消
     *
     * @return
     */
    boolean isSuccess();

}

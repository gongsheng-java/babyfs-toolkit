package com.babyfs.tk.commons.service;

import com.babyfs.tk.commons.service.annotation.InitStage;
import com.babyfs.tk.commons.service.annotation.ShutdownStage;

/**
 * 服务在启动和停止阶段执行的操作的注册器
 * <p/>
 *
 * @see {@link InitStage}
 * @see {@link ShutdownStage}
 */
public interface IStageActionRegistry {
    /**
     * @param action
     */
    public void addAction(Runnable action);

    /**
     * 执行所有的操作
     */
    public void execute();
}

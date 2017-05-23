package com.babyfs.tk.commons.service;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Injector;

/**
 * 应用全局服务的上下文
 */
public interface IContext {
    /**
     * 取得系统的启动参数,key:参数名称;value:参数值
     *
     * @return 可能为null
     */
    ImmutableMap<String, String> getAppArg();

    /**
     * 取得启动阶段的注册器
     *
     * @return
     */
    IStageActionRegistry getInitActionRegistry();

    /**
     * 取得启动后阶段的注册器
     * @return
     */
    IStageActionRegistry getAfterActionRegistry();

    /**
     * 取得停机阶段的注册器
     *
     * @return
     */
    IStageActionRegistry getShutdownActionRegistry();

    /**
     * 取得Guice的Inject对象,这个方法是为了一些hack功能,不推荐使用
     *
     * @return
     */
    Injector getInjector();


    /**
     * 设置Injector
     *
     * @param injector
     */
    void setInjector(Injector injector);

}

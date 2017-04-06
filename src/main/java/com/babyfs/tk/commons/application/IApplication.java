package com.babyfs.tk.commons.application;

import com.babyfs.tk.commons.service.ILifeService;
import com.google.common.util.concurrent.Service;
import com.google.inject.Module;

/**
 * 定义应用程序的接口
 */
public interface IApplication extends Service {
    /**
     *
     */
    String BIND_APP_ARGS_NAME = "app-args";

    /**
     * 取得Appliction的Modules
     *
     * @return
     */
    Iterable<? extends Module> getModules();

    /**
     * 取得Appliction的重载Modules
     *
     * @return
     */
    Iterable<? extends Module> getOverridingModules();

    /**
     * 增加一个service实例,适用用于无法通过{@link LifeServiceBindUtil#addLifeService}注册的服务
     *
     * @param service
     */
    void addILifeServvice(ILifeService service);

    /**
     * 根据参数<code>args</code>进行初始化,在初始化阶段需要完成如下工作:
     * <ul>
     * <li>初始化{@link #getModules()}</li>
     * <li>初始化{@link #getOverridingModules()}}</li>
     * </ul>
     *
     * @param args 参数
     */
    void init(String[] args);

    /**
     * 取得由{@link #init(String[])}设置的参数
     *
     * @return
     */
    String[] getArgs();
}

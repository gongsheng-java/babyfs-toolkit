package com.babyfs.tk.commons.name;

import com.babyfs.tk.commons.event.IEventListener;
import com.google.common.base.Function;

import java.util.List;

/**
 * 命名服务提供者接口
 */
public interface INameServiceProvider {
    /**
     * 重新加载所有的命名服务数据,加载完成后发送{@link NSProviderEventType#INIT} 事件向所有的监听者
     */
    void reload();

    /**
     * 初始化服务数据,成功加载后的服务器数据通过<code>function</code>回调接口通知给调用方
     *
     * @param function
     * @param <T>
     * @return
     */
    <T> T init(Function<List<Server>, T> function);

    /**
     * 增加事件监听器
     *
     * @param eventListener
     */
    void addListener(IEventListener<NSProviderEvent> eventListener);
}

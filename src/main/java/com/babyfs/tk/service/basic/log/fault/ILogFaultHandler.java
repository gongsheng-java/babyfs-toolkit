package com.babyfs.tk.service.basic.log.fault;

import com.babyfs.tk.service.basic.log.AbstractLogCollectMsg;

/**
 * 日志容错方法接口
 * <p/>
 */
public interface ILogFaultHandler {

    /**
     * 失败执行方法
     *
     * @param msg 日志信息
     */
    void handle(AbstractLogCollectMsg msg);

}

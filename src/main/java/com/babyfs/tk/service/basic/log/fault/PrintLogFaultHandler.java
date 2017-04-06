package com.babyfs.tk.service.basic.log.fault;

import com.babyfs.tk.service.basic.log.AbstractLogCollectMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 容错方法接口实现
 * <p/>
 * 使用{@link Logger} ERROR级别输出
 */
public class PrintLogFaultHandler implements ILogFaultHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrintLogFaultHandler.class);

    /**
     * 打印格式
     */
    private static final String PRINT_FORMAT = "[LOG_FAULT] %s";

    @Override
    public void handle(AbstractLogCollectMsg msg) {
        LOGGER.error(format(msg));
    }

    /**
     * 格式化结果集合
     *
     * @param msg 日志信息
     * @return
     */
    private String format(AbstractLogCollectMsg msg) {
        return String.format(PRINT_FORMAT, msg.toString());
    }

}

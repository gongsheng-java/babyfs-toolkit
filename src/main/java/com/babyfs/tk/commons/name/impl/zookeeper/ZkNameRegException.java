package com.babyfs.tk.commons.name.impl.zookeeper;

/**
 * 服务注册异常
 */
public class ZkNameRegException extends RuntimeException {
    public ZkNameRegException() {
    }

    public ZkNameRegException(String message) {
        super(message);
    }

    public ZkNameRegException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZkNameRegException(Throwable cause) {
        super(cause);
    }
}

package com.babyfs.tk.service.basic.probe;

/**
 * Probe数据发送者
 */
public interface IProbeSender {
    /**
     * 发送probe数据
     *
     * @param probeName
     * @param message
     */
    void send(String probeName, String message);
}

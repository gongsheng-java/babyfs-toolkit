package com.babyfs.tk.service.basic.queue;

/**
 * 队列接口
 */
public interface IQueue<T> {
    /**
     * 取得队列的名称
     *
     * @return
     */
    public String getName();

    /**
     * 向队列中插入一条消息
     *
     * @param value
     */
    public void put(T value);

    /**
     * 从队列中取得一条消息
     *
     * @return
     */
    public T get();
}

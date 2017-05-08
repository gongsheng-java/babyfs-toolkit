package com.babyfs.tk.service.basic.mq.kafka;

/**
 * Kafka消息处理器
 */
public interface IKafkaMsgProcessor<K, V> {
    /**
     * 处理指定的消息,需要注意
     * 由于Kafka的机制,在某些情况下会产生重复消息的,对于严格的应用场景,需要自行判断是否时重复的消息及其处理机制
     *
     * @param topic
     * @param key
     * @param message
     * @param retryCount 重试的次数
     * @return true, 处理成功;false,处理失败
     */
    boolean process(String topic, K key, V message, long retryCount);
}

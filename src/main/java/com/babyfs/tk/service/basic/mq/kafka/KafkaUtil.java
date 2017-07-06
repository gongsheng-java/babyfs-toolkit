package com.babyfs.tk.service.basic.mq.kafka;

/**
 * Kafka的工具类
 */
public class KafkaUtil {
    /**
     * 计算指定group的consumer offset part
     *
     * @param group
     * @param consumerOffsetCount
     * @return
     */
    public static int groupConsumerOffsetPart(String group, int consumerOffsetCount) {
        return Math.abs(group.hashCode()) % consumerOffsetCount;
    }
}

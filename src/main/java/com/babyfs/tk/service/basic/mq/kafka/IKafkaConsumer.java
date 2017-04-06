package com.babyfs.tk.service.basic.mq.kafka;

import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * consumer的实现，用于从broker中取数据
 * <p/>
 */
public interface IKafkaConsumer<K, V> {
    /**
     * 取得kafka consumer 的group id
     *
     * @return
     */
    String getGroupId();

    /**
     * 取得kafka的consumer的stream,key是topic
     *
     * @return
     */
    Map<String, List<KafkaConsumer<K, V>>> getTopicAndConsumers();


    /**
     * 取得consumer的配置
     *
     * @return
     */
    Properties getConfig();

    /**
     * 是否自动提交offeset
     *
     * @return
     */
    boolean isAutoCommitEnable();

    /**
     * 是否严格执行模式,在严格执行模式下,如果某条消息处理不成功,会停止处理后续的消息
     *
     * @return true, 严格模式;false,宽松模式
     */
    boolean isStriceMode();

    /**
     * 唤醒consumer
     *
     * @return
     */
    void wakeup();
}

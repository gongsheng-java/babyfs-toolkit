package com.babyfs.tk.service.basic.mq.kafka;

import java.util.List;
import java.util.Properties;

/**
 * kafka的生产者接口
 * <p/>
 */
public interface IKafkaProducer<K, V> {
    /**
     * 向kafka中send数据
     *
     * @param topic
     * @param data
     * @return true, 发送成功;false,发送失败
     */
    public boolean send(String topic, V data);

    /**
     * 向kafka中send数据
     *
     * @param topic
     * @param datas
     */
    public boolean send(String topic, List<V> datas);

    /**
     * 向kafka中send数据
     *
     * @param topic
     * @param key   If key is null, then it picks a random partition.
     * @param data
     */
    public boolean send(String topic, K key, V data);

    /**
     * 向kafka中send数据
     *
     * @param topic
     * @param key   If key is null, then it picks a random partition.
     * @param datas
     */
    public boolean send(String topic, K key, List<V> datas);

    /**
     * 取得kafkaProducer的名字
     *
     * @return
     */
    public String getName();

    /**
     * 关闭producer
     *
     * @return
     */
    public void shutdown();

    /**
     * 是否是同步发送
     */
    public boolean isSync();

    /**
     * 取得配置
     *
     * @return
     */
    public Properties getConfig();
}

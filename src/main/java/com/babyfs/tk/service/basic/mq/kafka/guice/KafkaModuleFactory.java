package com.babyfs.tk.service.basic.mq.kafka.guice;

import com.babyfs.tk.service.basic.guice.SimpleBasicServiceModule;
import com.babyfs.tk.service.basic.mq.kafka.IKafkaConsumer;
import com.babyfs.tk.service.basic.mq.kafka.IKafkaProducer;


/**
 * Kafka Producer Module  工厂类
 */
public final class KafkaModuleFactory {
    private KafkaModuleFactory() {

    }

    /**
     * 建立一个无名的Producer Module
     *
     * @param conf Producer配置文件名
     * @return
     */
    public static SimpleBasicServiceModule<IKafkaProducer> createProducerModule(String conf) {
        return new SimpleBasicServiceModule<>(conf, IKafkaProducer.class, ProducerProvider.class, null);
    }

    /**
     * 创建一个带有名称的Producer Module
     *
     * @param conf Producer配置文件名
     * @param name Producer名称
     * @return
     */
    public static SimpleBasicServiceModule<IKafkaProducer> createProducerModule(String conf, String name) {
        return new SimpleBasicServiceModule<>(conf, IKafkaProducer.class, ProducerProvider.class, name);
    }


    /**
     * 建立一个无名的Consumer Module
     *
     * @param conf Consumer配置文件名
     * @return
     */
    public static SimpleBasicServiceModule<IKafkaConsumer> createConsumerModule(String conf) {
        return new SimpleBasicServiceModule<>(conf, IKafkaConsumer.class, ConsumerProvider.class, null);
    }

    /**
     * 创建一个带有名称的Consumer Module
     *
     * @param conf Consumer配置文件名
     * @param name Consumer名称
     * @return
     */
    public static SimpleBasicServiceModule<IKafkaConsumer> createConsumerModule(String conf, String name) {
        return new SimpleBasicServiceModule<>(conf, IKafkaConsumer.class, ConsumerProvider.class, name);
    }
}

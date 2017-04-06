package com.babyfs.tk.service.basic.mq.kafka.guice;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.babyfs.tk.service.basic.guice.annotation.ServiceConf;
import com.babyfs.tk.service.basic.mq.kafka.IKafkaConsumer;
import com.babyfs.tk.service.basic.mq.kafka.internal.KafkaConsumerImpl;

import java.util.Map;

/**
 * {@link IKafkaConsumer}的提供者
 */
public class ConsumerProvider implements Provider<IKafkaConsumer> {
    /**
     * Producer 配置
     */
    @Inject
    @ServiceConf
    private Map<String, String> conf;


    @Override
    public IKafkaConsumer get() {
        return new KafkaConsumerImpl(conf);
    }
}

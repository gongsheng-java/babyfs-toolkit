package com.babyfs.tk.service.basic.mq.kafka.guice;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.babyfs.tk.commons.service.IContext;
import com.babyfs.tk.service.basic.guice.annotation.ServiceConf;
import com.babyfs.tk.service.basic.mq.kafka.IKafkaProducer;
import com.babyfs.tk.service.basic.mq.kafka.impl.KafkaProducerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Kafka Producer提供者
 */
public class ProducerProvider implements Provider<IKafkaProducer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProducerProvider.class);
    /**
     * Producer 配置
     */
    @Inject
    @ServiceConf
    private Map<String, String> conf;

    /**
     * 服务的上下文
     */
    @Inject(optional = true)
    private IContext context;


    @Override
    public IKafkaProducer get() {
        final IKafkaProducer kafkaProducer = new KafkaProducerImpl(this.conf);
        if (this.context != null) {
            LOGGER.info("Add Producer {} to ShutdownActionRegistry", kafkaProducer);
            context.getShutdownActionRegistry().addAction(new Runnable() {
                @Override
                public void run() {
                    kafkaProducer.shutdown();
                }
            });
        } else {
            LOGGER.warn("Not found ShutdownActionRegistry,the producer will not be closed when the jvm shutdown.");
        }
        return kafkaProducer;
    }
}

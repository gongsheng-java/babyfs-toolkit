package com.babyfs.tk.service.basic.probe;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.babyfs.tk.service.basic.mq.kafka.IKafkaProducer;
import org.apache.commons.lang.StringUtils;

/**
 * 将Probe数据发送到Kafka中,完整的数据流:
 * <pre>
 * Probe - Kafka - Logstash - ElasticSearch - kibana
 * </pre>
 */
public class KafkaProbeSender implements IProbeSender {
    private final IKafkaProducer producer;
    private final String topic;

    /**
     * @param producer Kafka Producer,必须是async
     * @param topic    topic的名称,非空
     */
    public KafkaProbeSender(IKafkaProducer producer, String topic) {
        this.producer = Preconditions.checkNotNull(producer, "The producer must not be null");
        this.topic = Preconditions.checkNotNull(StringUtils.trimToNull(topic));
        Preconditions.checkArgument(!this.producer.isSync(), "The send mode of kafka producer must be async");
    }

    public IKafkaProducer getProducer() {
        return producer;
    }

    public String getTopic() {
        return topic;
    }

    @Override
    public void send(String probeName, String message) {
        Preconditions.checkNotNull(probeName);
        if (Strings.isNullOrEmpty(message)) {
            return;
        }
        this.producer.send(topic, probeName, message);
    }
}

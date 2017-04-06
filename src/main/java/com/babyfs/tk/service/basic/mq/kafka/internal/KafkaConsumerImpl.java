package com.babyfs.tk.service.basic.mq.kafka.internal;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.babyfs.tk.service.basic.mq.kafka.IKafkaConsumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * kafka的consumer的实现类
 * <p/>
 */
public class KafkaConsumerImpl<K, V> implements IKafkaConsumer<K, V> {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumerImpl.class);
    private static final String TOPIC_CONFIG_PREFIX = "_topic.";
    private static final String CONSUMER_CONFIG_PREFIEX = "_consumer.";
    private static final String GROUP_ID = "group.id";
    private static final String ENABLE_AUTO_COMMIT = "enable.auto.commit";
    private static final String CONSUMER_CONFIG_STRICT_MODE = CONSUMER_CONFIG_PREFIEX + "strict_mode";

    /**
     * 消费者配置
     */
    private final Properties config;

    /**
     * 客户端连接
     */
    private Map<String, List<KafkaConsumer<K, V>>> messageConsumers = Maps.newHashMap();
    /**
     * consumer group id
     */
    private final String groupId;
    /**
     * 是否自动提交
     */
    private final boolean autoCommitEnable;
    /**
     * 是否严格模式
     */
    private final boolean strictMode;

    public KafkaConsumerImpl(@Nonnull Map<String, String> conf) {
        Map<String, String> consumerConfigMap = Maps.filterKeys(conf, input -> !input.startsWith(TOPIC_CONFIG_PREFIX) && !input.startsWith(CONSUMER_CONFIG_PREFIEX));
        config = new Properties();
        config.putAll(consumerConfigMap);
        groupId = this.config.getProperty(GROUP_ID);
        Map<String, String> topicConfig = Maps.filterKeys(conf, input -> {
            return input.startsWith(TOPIC_CONFIG_PREFIX);
        });
        this.autoCommitEnable = "true".equalsIgnoreCase(this.config.getProperty(ENABLE_AUTO_COMMIT));
        this.strictMode = "true".equalsIgnoreCase(conf.get(CONSUMER_CONFIG_STRICT_MODE));
        LOGGER.info("autocommit:{},strict_mode:{}", this.autoCommitEnable, this.strictMode);
        init(topicConfig);
    }

    @Override
    public synchronized void wakeup() {
        Map<String, List<KafkaConsumer<K, V>>> temp = this.messageConsumers;
        for (List<KafkaConsumer<K, V>> consumers : temp.values()) {
            for (KafkaConsumer<K, V> consumer : consumers) {
                consumer.wakeup();
            }
        }
    }

    @Override
    public String getGroupId() {
        return groupId;
    }

    /**
     * 取得kafka的consumer的stream,key是topic
     *
     * @return
     */
    @Override
    public Map<String, List<KafkaConsumer<K, V>>> getTopicAndConsumers() {
        return Collections.unmodifiableMap(this.messageConsumers);
    }

    /**
     * topicConfigValue的格式是JSON,具体的格式请参考test/resourcers/kafka-consumer.xml
     *
     * @param topicConfig
     */
    private void init(Map<String, String> topicConfig) {

        Map<String, Integer> topicCountMap = Maps.newHashMap();
        for (Map.Entry<String, String> entry : topicConfig.entrySet()) {
            String topicName = entry.getKey().substring(TOPIC_CONFIG_PREFIX.length());
            String topicConfigValue = entry.getValue();
            KafkaConsumer<K, V> consumer = new KafkaConsumer<K, V>(this.config);
            consumer.subscribe(Lists.<String>newArrayList(topicName));
            JSONObject jsonObject = JSONObject.parseObject(topicConfigValue);
            Integer threads = jsonObject.getInteger("threads");
            Preconditions.checkArgument(threads != null, "Can't find the `threads` config for topic %s", topicName);
            topicCountMap.put(topicName, threads);
            LOGGER.info("Create consummer for topic {},thread count:{}", topicName, threads);
        }


        Map<String, List<KafkaConsumer<K, V>>> topicStreams = Maps.newHashMap();
        for (Map.Entry<String, Integer> entry : topicCountMap.entrySet()) {
            String topicName = entry.getKey();
            int threads = entry.getValue();
            List<KafkaConsumer<K, V>> consumers = Lists.newArrayList();
            for (int i = 0; i < threads; i++) {
                KafkaConsumer<K, V> consumer = new KafkaConsumer<K, V>(this.config);
                consumer.subscribe(Lists.newArrayList(topicName));
                consumers.add(consumer);
            }
            topicStreams.put(topicName, consumers);
        }
        this.messageConsumers.putAll(topicStreams);
    }

    @Override
    public Properties getConfig() {
        return config;
    }

    @Override
    public boolean isAutoCommitEnable() {
        return autoCommitEnable;
    }

    @Override
    public boolean isStriceMode() {
        return this.strictMode;
    }
}

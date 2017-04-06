package com.babyfs.tk.service.basic.mq.kafka;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.babyfs.tk.commons.service.LifeServiceSupport;
import com.babyfs.tk.commons.thread.NamedThreadFactory;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Kafka Consumer Worker
 */
public class KafkaConsumerWorkerService extends LifeServiceSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumerWorkerService.class);
    private final String name;
    private final Map<String, IKafkaMsgProcessor> topicProcessorMap = Maps.newHashMap();
    private final List<IKafkaConsumer> consumers = Lists.newArrayList();
    private final List<ConsumerRunner> consumerRunner = Lists.newArrayList();

    /**
     * @param name              服务名称
     * @param consumers         Consumer列表
     * @param topicProcessorMap topic的处理对象Map,key:topic name,value:processor
     * @param regShutdownHook   是否注册JVM shutdown hoook:
     *                          true,注册shutdown hook,jvm停止时,调用{@link #stopAsync()#awaitTerminated()}停止服务;
     *                          false,不注册shutdown hook,这时需要由调用者负责在JVM停止的时候停止服务
     */
    public KafkaConsumerWorkerService(@Nonnull String name, List<IKafkaConsumer> consumers, @Nonnull Map<String, IKafkaMsgProcessor> topicProcessorMap, boolean regShutdownHook) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(name), "name can't be null or empty.");
        Preconditions.checkArgument(consumers != null && !consumers.isEmpty(), "consumbers can't be null or empty");
        Preconditions.checkArgument(topicProcessorMap != null && !topicProcessorMap.isEmpty(), "topicProcessorMap can't be null or empty");
        this.name = name;
        this.topicProcessorMap.putAll(topicProcessorMap);
        this.consumers.addAll(consumers);
        if (regShutdownHook) {
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    KafkaConsumerWorkerService.this.stopAsync().awaitTerminated();
                }
            });
        }
    }

    /**
     * 启动consumer
     */
    @Override
    protected synchronized void execStart() {
        LOGGER.info("Starting Kafka Worker:{}", this.getName());
        for (IKafkaConsumer consumer : this.consumers) {
            Map<String, List<KafkaConsumer>> topicAndStreams = consumer.getTopicAndConsumers();
            for (Map.Entry<String, List<KafkaConsumer>> entry : topicAndStreams.entrySet()) {
                String topic = entry.getKey();
                List<KafkaConsumer> kafkaStreams = entry.getValue();
                Preconditions.checkState(this.topicProcessorMap.containsKey(topic), "Can't find the IKafkaMessageProcessor for topic:%s", topic);
                Preconditions.checkState(!kafkaStreams.isEmpty(), "Can't find the KafkaStream for topic:%s", topic);
            }
            consumerRunner.add(new ConsumerRunner(consumer));
        }
        for (ConsumerRunner runner : consumerRunner) {
            runner.start();
        }
        LOGGER.info("Started Kafka Worker:{}", this.getName());
    }

    @Override
    protected synchronized void execStop() {
        LOGGER.info("Stopping Kafka Worker:{}", this.getName());
        try {
            for (ConsumerRunner runner : consumerRunner) {
                runner.willStop();
            }
        } catch (Exception e) {
            LOGGER.error("try stop consumer runner error.", e);
        }
        try {
            for (IKafkaConsumer consumer : this.consumers) {
                consumer.wakeup();
            }
        } catch (Exception e) {
            LOGGER.error("wakeup consumer error.", e);
        }
        try {
            for (ConsumerRunner runner : consumerRunner) {
                runner.stop();
            }
        } catch (Exception e) {
            LOGGER.error("stop consumer runner error.", e);
        }
        LOGGER.info("Stopped Kafka Worker:{}", this.getName());
    }

    @Override
    public String getName() {
        return name;
    }

    public class ConsumerRunner {
        private IKafkaConsumer consumer;
        private ExecutorService executor;
        private volatile boolean stop = false;

        public ConsumerRunner(IKafkaConsumer consumer) {
            this.consumer = consumer;
        }

        public synchronized void start() {
            Preconditions.checkState(this.executor == null, "The executor shoud be null");
            LOGGER.info("Starting Kafka consumer group {}", consumer.getGroupId());
            this.stop = false;
            this.executor = Executors.newCachedThreadPool(new NamedThreadFactory("kafka-consumer-" + this.consumer.getGroupId()));
            Map<String, List<KafkaConsumer>> topicAndStreams = consumer.getTopicAndConsumers();
            for (Map.Entry<String, List<KafkaConsumer>> entry : topicAndStreams.entrySet()) {
                String topic = entry.getKey();
                List<KafkaConsumer> kafkaStreams = entry.getValue();
                IKafkaMsgProcessor processor = topicProcessorMap.get(topic);
                for (KafkaConsumer stream : kafkaStreams) {
                    LOGGER.info("Starting Kafka consumer group {},topic:{}", consumer.getGroupId(), topic);
                    KafkaMessageTask task = new KafkaMessageTask(stream, processor, consumer.getGroupId(), topic, consumer.isAutoCommitEnable(), consumer.isStriceMode());
                    this.executor.submit(task);
                }
            }
            LOGGER.info("Started Kafka consumer group {}", consumer.getGroupId());
        }

        synchronized void willStop() {
            LOGGER.info("Will stop Kafka consumer group {}", consumer.getGroupId());
            this.stop = true;
        }

        synchronized void stop() {
            Preconditions.checkState(this.executor != null, "The executor shoud not be null");
            LOGGER.info("Stopping Kafka consumer group {}", consumer.getGroupId());
            this.stop = true;
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                    if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                        LOGGER.warn("executor did not terminate");
                    }
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            executor = null;
            LOGGER.info("Stopped Kafka consumer group {}", consumer.getGroupId());
        }

        class KafkaMessageTask<K, V> implements Runnable {
            private final IKafkaMsgProcessor<K, V> processor;
            private final KafkaConsumer<K, V> stream;
            private final String groupId;
            private final String topic;
            private final boolean autoCommit;
            private final boolean strictMode;


            KafkaMessageTask(final KafkaConsumer<K, V> stream, final IKafkaMsgProcessor<K, V> processor, String groupId, String topic, boolean autoCommit, boolean strictMode) {
                this.stream = stream;
                this.processor = processor;
                this.groupId = groupId;
                this.topic = topic;
                this.autoCommit = autoCommit;
                this.strictMode = strictMode;
            }


            @Override
            public void run() {
                LOGGER.info("Process topic:{},autocommit:{},strict mode:{},group id:{},begin", topic, autoCommit, strictMode, this.groupId);

                try {
                    while (!stop) {
                        ConsumerRecords<K, V> records = this.stream.poll(Long.MAX_VALUE);
                        for (TopicPartition partition : records.partitions()) {
                            List<ConsumerRecord<K, V>> recordList = records.records(partition);
                            for (ConsumerRecord<K, V> record : recordList) {
                                String topic = record.topic();
                                K key = record.key();
                                V message = record.value();
                                //在严格模式下,要确保消息确实被处理了,一直尝试重试,直到消息被消费了成功了
                                long reTryCount = 0;
                                boolean processRet = false;
                                final JSONObject topicMsg = new JSONObject();
                                topicMsg.put("topic", topic);
                                topicMsg.put("message", message);
                                do {
                                    try {
                                        processRet = processor.process(topic, key, message, reTryCount++);
                                    } catch (Throwable e) {
                                        LOGGER.error("Process message error for topic " + topic, e);
                                    }
                                    if (!processRet) {
                                        if (strictMode && !stop) {
                                            if (reTryCount % 5 == 1) {
                                                LOGGER.warn("Process message fail,retry:{} {}", reTryCount, topicMsg.toJSONString());
                                            }
                                            try {
                                                Thread.sleep(50);
                                            } catch (InterruptedException e) {
                                                LOGGER.error("Thread interrupted", e);
                                                Thread.currentThread().interrupt();
                                            }
                                        }
                                    } else {
                                        break;
                                    }
                                } while (strictMode && !stop);

                                if (!processRet) {
                                    LOGGER.warn("Process message error:{}", topicMsg.toJSONString());
                                }

                                if (strictMode) {
                                    if (!processRet && !stop) {
                                        String msg = "The processRet and stop are both false in strict mode,which should not happen,may be it's a bug";
                                        LOGGER.error(msg);
                                        throw new IllegalStateException(msg);
                                    }
                                }

                                if (autoCommit) {
                                    continue;
                                }

                                reTryCount = 0;
                                long offset = record.offset();
                                boolean commitOffsetRet = false;
                                String msg = "Update topic:" + topic + " partition:" + partition + " offset:" + offset;
                                do {
                                    try {
                                        this.stream.commitSync(Collections.singletonMap(partition, new OffsetAndMetadata(offset + 1)));
                                        commitOffsetRet = true;
                                    } catch (Exception e) {
                                        LOGGER.error(msg, e);
                                    }
                                    if (commitOffsetRet) {
                                        break;
                                    } else {
                                        reTryCount++;
                                        //最多等待10秒,如果更新一直失败,那么会导致消息重复被处理
                                        if (reTryCount <= 10) {
                                            try {
                                                Thread.sleep(1000);
                                            } catch (InterruptedException ie) {
                                                LOGGER.error("Thread interrupted", ie);
                                                Thread.currentThread().interrupt();
                                            }
                                        } else {
                                            LOGGER.warn(msg + " reTryCount:" + reTryCount + ",skip it");
                                            break;
                                        }
                                    }
                                } while (!stop);
                            }
                        }
                    }
                    LOGGER.info("Process topic {} strict mode {},finished", topic, strictMode);
                } finally {
                    this.stream.close();
                }
            }
        }
    }
}

package com.babyfs.tk.service.basic.mq.kafka.impl;

import com.babyfs.tk.service.basic.mq.kafka.IKafkaProducer;
import com.babyfs.tk.service.basic.mq.kafka.KafkaConfs;
import com.google.common.collect.Lists;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;

/**
 * kafka生产者的实现
 * <p/>
 */
public class KafkaProducerImpl<K, V> implements IKafkaProducer<K, V> {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaProducerImpl.class);
    public static final String ASYNC = "async";
    public static final String SYNC = "sync";
    private static final String CLIENT_ID = "client.id";
    private static final String PRODUCER_TYPE = "_producer.type";

    private Properties config;
    private String name;
    private KafkaProducer<K, V> producer;
    private boolean sync;

    private ReentrantLock newProducerLock = new ReentrantLock();
    private volatile boolean hasInitProducer = false;

    @SuppressWarnings("unchecked")
    public KafkaProducerImpl(@Nonnull Map<String, String> conf) {
        config = new Properties();
        config.putAll(conf);
        KafkaConfs.setDefaultConfs(config);

        this.name = config.getProperty(CLIENT_ID);

    }


    @Override
    public String getName() {
        return this.name;
    }


    private void lazyNewProducer(){
        if(hasInitProducer){
            return;
        }
        newProducerLock.lock();
        try{
            if(hasInitProducer) {
                return;
            }
            LOGGER.info("try to connect to kafka");
            String producerType = config.getProperty(PRODUCER_TYPE);
            config.remove(PRODUCER_TYPE);
            if (ASYNC.equals(producerType)) {
                //异步Producer
                this.producer = new KafkaProducer<>(config);
                sync = false;
            } else if (SYNC.equals(producerType)) {
                //同步Producer
                this.producer = new KafkaProducer<>(config);
                sync = true;
            } else {
                throw new IllegalArgumentException("Unknown producer tyep:" + producerType);
            }
            KafkaProducerTraceUtil.init();
            hasInitProducer = true;
        }finally {
            newProducerLock.unlock();
        }
    }

    /**
     * 向kafka中send数据
     *
     * @param topic
     * @param key   If key is null, then it picks a random partition.
     * @param datas
     */
    @Override
    public boolean send(final String topic, final K key, final List<V> datas) {
        lazyNewProducer();
        try {
            for (V data : datas) {
                ProducerRecord<K, V> record = new ProducerRecord<K, V>(topic, key, data);
                if (this.sync) {
                    producer.send(record).get();
                } else {
                    producer.send(record, new Callback() {
                        @Override
                        public void onCompletion(RecordMetadata metadata, Exception exception) {
                            if (exception != null) {
                                LOGGER.error("Send to topic " + topic + " fail.", exception);
                                KafkaProducerTraceUtil.logSendFail(topic, key, data);
                            }
                        }
                    });
                }
            }
            return true;
        } catch (Throwable e) {
            KafkaProducerTraceUtil.logSendFail(topic, key, datas);
            LOGGER.error("Send to topic " + topic + " fail.", e);
            if (!(e instanceof Exception)) {
                //如果不是Exception,可能是Error,这种错误应该重新抛出给上层
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    /**
     * 向kafka中send数据
     *
     * @param topic
     * @param datas
     */
    @Override
    public boolean send(String topic, List<V> datas) {
        return this.send(topic, null, datas);
    }

    @Override
    public boolean send(String topic, K key, V data) {
        return this.send(topic, key, Lists.newArrayList(data));
    }

    /**
     * 向kafka中send数据
     *
     * @param topic
     * @param data
     */
    @Override
    public boolean send(String topic, V data) {
        return this.send(topic, Lists.newArrayList(data));
    }

    /**
     * 关闭producer
     *
     * @return
     */
    @Override
    public void shutdown() {
        // 添加shutdownHook,当结束使用时会把队列中的数据发完再结束程序
        try {
            producer.close();
        } catch (Exception e) {
            LOGGER.error("failed shutdown kafka producer {} connector.", this.getName(), e);
        }
    }

    @Override
    public boolean isSync() {
        return sync;
    }

    @Override
    public Properties getConfig() {
        return this.config;
    }
}

package com.babyfs.tk.service.basic.kafka;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.babyfs.tk.commons.xml.XmlProperties;
import com.babyfs.tk.service.basic.mq.kafka.IKafkaConsumer;
import com.babyfs.tk.service.basic.mq.kafka.internal.KafkaConsumerImpl;
import com.babyfs.tk.service.basic.mq.kafka.IKafkaMsgProcessor;
import com.babyfs.tk.service.basic.mq.kafka.KafkaConsumerWorkerService;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Map;

public class KafkaConsumerWorkerServiceTest {
    @Test
    @Ignore
    public void testWorker() throws InterruptedException {
        IKafkaConsumer consumer = new KafkaConsumerImpl(XmlProperties.loadFromXml("kafka-consumer.xml"));
        Map<String, IKafkaMsgProcessor> processorMap = Maps.newHashMap();
        processorMap.put("test", new EchoMsgMsgProcessor());
        processorMap.put("test2", new EchoMsgMsgProcessor());
        KafkaConsumerWorkerService workerService = new KafkaConsumerWorkerService("test", Lists.newArrayList(consumer), processorMap, true);
        workerService.startAsync().awaitRunning();
        Thread.sleep(600 * 1000);
        //workerService.stopAsync().awaitTerminated();
    }

    @Test
    @Ignore
    public void testWorkerNoAuto() throws InterruptedException {
        IKafkaConsumer consumer = new KafkaConsumerImpl(XmlProperties.loadFromXml("kafka-consumer-noauto.xml"));
        Map<String, IKafkaMsgProcessor> processorMap = Maps.newHashMap();
        processorMap.put("test", new EchoMsgMsgProcessor());
        processorMap.put("test2", new EchoMsgMsgProcessor());
        KafkaConsumerWorkerService workerService = new KafkaConsumerWorkerService("test", Lists.newArrayList(consumer), processorMap, true);
        workerService.startAsync().awaitRunning();
        Thread.sleep(600 * 1000);
        //workerService.stopAsync().awaitTerminated();
    }

    public static class EchoMsgMsgProcessor implements IKafkaMsgProcessor {
        @Override
        public boolean process(String topic, Object key, Object message,long retryCount) {
            System.out.println(topic + " " + key + " " + message);
            return true;
        }
    }
}

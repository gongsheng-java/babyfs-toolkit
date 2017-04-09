package com.babyfs.tk.service.basic.kafka;

import com.babyfs.tk.service.basic.mq.kafka.IKafkaConsumer;
import com.babyfs.tk.service.basic.mq.kafka.IKafkaProducer;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.babyfs.tk.service.basic.guice.BasicServiceModule;
import com.babyfs.tk.service.basic.guice.SimpleBasicServiceModule;
import com.babyfs.tk.service.basic.mq.kafka.guice.KafkaModuleFactory;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class KafkaServiceModuleFactoryTest {

    @Test
    @Ignore
    public void testSync() throws Exception {
        BasicServiceModule module = KafkaModuleFactory.createProducerModule("kafka-producer-sync.xml");
        Injector injector = Guice.createInjector(module);
        IKafkaProducer<String, String> producer = injector.getInstance(IKafkaProducer.class);
        Assert.assertTrue(producer.isSync());
        for (int i = 0; i < 100; i++) {
            producer.send("test", "k" + i, "v" + i);
        }
        producer.shutdown();
    }

    @Test
    @Ignore
    public void testASync() throws Exception {
        BasicServiceModule module = KafkaModuleFactory.createProducerModule("kafka-producer-async.xml");
        Injector injector = Guice.createInjector(module);
        IKafkaProducer<String, String> producer = injector.getInstance(IKafkaProducer.class);
        Assert.assertFalse(producer.isSync());
        producer.send("test", "k", "def");
        producer.shutdown();
    }

    @Test
    @Ignore
    public void testNamedSync() throws Exception {
        BasicServiceModule p0 = KafkaModuleFactory.createProducerModule("kafka-producer-sync.xml", "sync");
        BasicServiceModule p1 = KafkaModuleFactory.createProducerModule("kafka-producer-async.xml", "async");
        Injector injector = Guice.createInjector(p0, p1);
        Key<IKafkaProducer> syncName = Key.get(IKafkaProducer.class, Names.named("sync"));
        Key<IKafkaProducer> asyncName = Key.get(IKafkaProducer.class, Names.named("async"));
        IKafkaProducer<String, String> syncProducer = injector.getInstance(syncName);
        IKafkaProducer<String, String> asyncProducer = injector.getInstance(asyncName);
        Assert.assertTrue(syncProducer.isSync());
        Assert.assertFalse(asyncProducer.isSync());
        syncProducer.send("unit-test", "k", "abc");
        syncProducer.send("test", "k", "def");
        syncProducer.shutdown();
        asyncProducer.shutdown();
    }

    @Test
    @Ignore
    public void testConsumer() {
        SimpleBasicServiceModule<IKafkaConsumer> consumerModule = KafkaModuleFactory.createConsumerModule("kafka-consumer.xml");
        Injector injector = Guice.createInjector(consumerModule);
        IKafkaConsumer instance = injector.getInstance(IKafkaConsumer.class);
        Assert.assertNotNull(instance);
        instance.wakeup();
    }
}

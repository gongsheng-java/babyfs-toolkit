package com.babyfs.tk.service.basic.guice;

import com.babyfs.tk.service.basic.probe.guice.KafkaProbeModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.babyfs.tk.service.basic.mq.kafka.IKafkaProducer;
import com.babyfs.tk.service.basic.probe.IProbeSender;
import com.babyfs.tk.service.basic.probe.KafkaProbeSender;
import org.junit.Assert;
import org.junit.Test;

public class KafkaProbeModuleTest {

    @Test
    public void testConfigure() throws Exception {
        String probeTopic = "probe_test";
        KafkaProbeModule module = new KafkaProbeModule(probeTopic, "kafka-producer-probe-async.xml");
        Injector injector = Guice.createInjector(module);

        IProbeSender probeSender = injector.getInstance(IProbeSender.class);
        Assert.assertNotNull(probeSender);

        Assert.assertTrue(probeSender instanceof KafkaProbeSender);
        KafkaProbeSender kafkaProbeSender = (KafkaProbeSender) probeSender;
        Assert.assertEquals(probeTopic, kafkaProbeSender.getTopic());

        IKafkaProducer producer = kafkaProbeSender.getProducer();
        Assert.assertNotNull(producer);
        Assert.assertFalse(producer.isSync());
        Assert.assertEquals("probe_client", producer.getName());
    }
}
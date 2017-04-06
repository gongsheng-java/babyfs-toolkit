package com.babyfs.tk.service.basic.probe.guice;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.PrivateModule;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.babyfs.tk.service.basic.mq.kafka.IKafkaProducer;
import com.babyfs.tk.service.basic.mq.kafka.guice.KafkaModuleFactory;
import com.babyfs.tk.service.basic.probe.Config;
import com.babyfs.tk.service.basic.probe.IProbeSender;
import com.babyfs.tk.service.basic.probe.KafkaProbeSender;
import com.babyfs.tk.service.basic.utils.BinderUtil;
import org.apache.commons.lang.StringUtils;


/**
 * 基于Kafka的Probe Module,这是一个私有的Module,不暴露任何key,只用来初始化ProbeService
 */
public class KafkaProbeModule extends PrivateModule {

    public static final String PROBE_TOPIC = "ProbeTopic";
    /**
     * Probe数据的日志队列
     */
    private final String probeTopic;
    /**
     * Probe数据需要的Kafka Producer
     */
    private final String probeKafkaProducerConf;
    /**
     * Probe的配置
     */
    private final Config config;

    /**
     * @param probeTopic
     * @param probeKafkaProducerConf
     */
    public KafkaProbeModule(String probeTopic, String probeKafkaProducerConf) {
        this(probeTopic, probeKafkaProducerConf, null);
    }

    /**
     * @param probeTopic
     * @param probeKafkaProducerConf
     * @param config
     */
    public KafkaProbeModule(String probeTopic, String probeKafkaProducerConf, Config config) {
        this.probeTopic = Preconditions.checkNotNull(StringUtils.trimToNull(probeTopic));
        this.probeKafkaProducerConf = Preconditions.checkNotNull(StringUtils.trimToNull(probeKafkaProducerConf));
        this.config = config;
    }

    @Override
    protected void configure() {
        if (this.config != null) {
            bind(Config.class).toInstance(this.config);
        }
        BinderUtil.bind(binder(), PROBE_TOPIC, probeTopic);
        bind(IProbeSender.class).toProvider(KafkaProbeSenderProvider.class).asEagerSingleton();
        install(KafkaModuleFactory.createProducerModule(probeKafkaProducerConf));
        expose(IProbeSender.class);
    }

    public static class KafkaProbeSenderProvider implements Provider<KafkaProbeSender> {
        @Inject
        IKafkaProducer kafkaProducer;

        @Inject
        @Named(PROBE_TOPIC)
        String probeTopic;

        @Override
        public KafkaProbeSender get() {
            return new KafkaProbeSender(kafkaProducer, probeTopic);
        }
    }
}

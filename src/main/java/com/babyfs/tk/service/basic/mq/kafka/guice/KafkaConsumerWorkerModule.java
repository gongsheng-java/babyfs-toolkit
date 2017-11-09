package com.babyfs.tk.service.basic.mq.kafka.guice;

import com.babyfs.tk.service.basic.mq.kafka.IKafkaConsumer;
import com.babyfs.tk.service.basic.mq.kafka.IKafkaMsgProcessor;
import com.babyfs.tk.service.basic.mq.kafka.KafkaConsumerWorkerService;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.*;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.babyfs.tk.commons.application.LifeServiceBindUtil;
import com.babyfs.tk.commons.xml.XmlProperties;
import com.babyfs.tk.service.basic.guice.SimpleBasicServiceModule;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Kafka消费者工作者模块,负责创建Consumer和Worker
 */
public class KafkaConsumerWorkerModule extends AbstractModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumerWorkerModule.class);
    private static final AtomicInteger WORKER_NAME_COUNTER = new AtomicInteger();
    private static final String NAME = "name";

    /**
     * 默认的Consumer配置文件
     */
    public static final String CONF_KAFKA_CONSUMER = "kafka-consumer.xml";
    /**
     * 默认的Topic Processor配置文件
     */
    public static final String CONF_KAFKA_TOPIC_PROCESSOR = "kafka-topic-processor.xml";
    /**
     * Worker的名称
     */
    public static final String DEFAULT_NAME = "KafkaConsumerWorker";

    private final String name;
    private final String confOfKafkaConsumer;
    private final String confOfTopicProcessor;
    private final Map<String, Object> topicProcessorMap;

    /**
     * 使用默认的配置
     */
    public KafkaConsumerWorkerModule() {
        this(CONF_KAFKA_CONSUMER, CONF_KAFKA_TOPIC_PROCESSOR, DEFAULT_NAME + "-" + WORKER_NAME_COUNTER.getAndIncrement());
    }

    /**
     * @param confOfKafkaConsumer  Kafka Consumer配置文件,非空
     * @param confOfTopicProcessor Kafka Topic处理的配置文件,非空
     * @param name                 Worker的名称,非空
     */
    public KafkaConsumerWorkerModule(String confOfKafkaConsumer, String confOfTopicProcessor, String name) {
        this(confOfKafkaConsumer, confOfTopicProcessor, name, null);
    }

    /**
     * @param confOfKafkaConsumer  Kafka Consumer配置文件,非空
     * @param confOfTopicProcessor Kafka Topic处理的配置文件
     * @param name                 Worker的名称,非空
     * @param topicProcessorMap    topic processor的配置,和{@code confOfTopicProcessor}一样,用于配置topic的Processor.
     *                             {@code topicProcessorMap}和{@code confOfTopicPrcessor}不能同时为空,即必须有一个配置.
     *                             {@code topicProcessorMap}的value允许的类型有:
     *                             <ul>
     *                             <li>String,可以是一个{@link IKafkaMsgProcessor}的类型,或者Provider的类名</li>
     *                             <li>Class,必须是{@link IKafkaMsgProcessor}或者Provider的子类</li>
     *                             <li>{@link IKafkaMsgProcessor}或者{@link Provider}的实例</li>
     *                             <li>其他值会抛出运行时异常</li>
     *                             </ul>
     */
    public KafkaConsumerWorkerModule(String confOfKafkaConsumer, String confOfTopicProcessor, String name, Map<String, Object> topicProcessorMap) {
        this.confOfKafkaConsumer = Preconditions.checkNotNull(confOfKafkaConsumer);
        this.confOfTopicProcessor = StringUtils.trimToNull(confOfTopicProcessor);
        this.name = Preconditions.checkNotNull(name);
        this.topicProcessorMap = topicProcessorMap;
        Preconditions.checkArgument(this.confOfTopicProcessor != null || this.topicProcessorMap != null, "No configured topic processor");
    }


    @Override
    protected void configure() {
        final Key<KafkaConsumerWorkerService> kafkaConsumerWorkerServiceKey = Key.get(KafkaConsumerWorkerService.class);
        Module processorModule = new PrivateProcessorModule(kafkaConsumerWorkerServiceKey);
        install(processorModule);
        LifeServiceBindUtil.addLifeService(binder(), kafkaConsumerWorkerServiceKey);
    }

    public static class KafkaConsumerWorkerServiceProvider implements Provider<KafkaConsumerWorkerService> {
        @Inject
        private Map<String, IKafkaMsgProcessor> topicMsgProcessor;

        @Inject
        private IKafkaConsumer consumer;

        @Inject
        @Named(NAME)
        private String name;

        @Override
        public KafkaConsumerWorkerService get() {
            return new KafkaConsumerWorkerService(name, Lists.newArrayList(consumer), topicMsgProcessor, true);
        }
    }

    private class PrivateProcessorModule extends PrivateModule {
        private final Key<KafkaConsumerWorkerService> kafkaConsumerWorkerServiceKey;

        public PrivateProcessorModule(Key<KafkaConsumerWorkerService> kafkaConsumerWorkerServiceKey) {
            this.kafkaConsumerWorkerServiceKey = kafkaConsumerWorkerServiceKey;
        }

        @Override
        protected void configure() {
            SimpleBasicServiceModule<IKafkaConsumer> consumerModule = KafkaModuleFactory.createConsumerModule(confOfKafkaConsumer);
            install(consumerModule);
            MapBinder<String, IKafkaMsgProcessor> processorMapBinder = MapBinder.newMapBinder(binder(), String.class, IKafkaMsgProcessor.class);
            if (confOfTopicProcessor != null) {
                Map<String, String> topicProcessorMmap = XmlProperties.loadFromXml(confOfTopicProcessor);
                Preconditions.checkState(topicProcessorMmap != null && !topicProcessorMmap.isEmpty(), "Can't load topic processors from %s", confOfTopicProcessor);
                buildProcessorBinder(processorMapBinder, topicProcessorMmap);
            }
            if (topicProcessorMap != null) {
                buildProcessorBinder(processorMapBinder, topicProcessorMap);
            }
            bind(String.class).annotatedWith(Names.named(NAME)).toInstance(name);
            bind(kafkaConsumerWorkerServiceKey).toProvider(KafkaConsumerWorkerServiceProvider.class).asEagerSingleton();
            expose(kafkaConsumerWorkerServiceKey);
        }

        /**
         *
         * @param processorMapBinder
         * @param topicProcessorMmap
         */
        private void buildProcessorBinder(MapBinder<String, IKafkaMsgProcessor> processorMapBinder, Map<String, ? extends Object> topicProcessorMmap) {
            for (Map.Entry<String, ? extends Object> entry : topicProcessorMmap.entrySet()) {
                String key = Preconditions.checkNotNull(StringUtils.trimToNull(entry.getKey()));
                Object value = Preconditions.checkNotNull(entry.getValue());
                if (value instanceof String) {
                    try {
                        Class loadedClass = Class.forName((String) value);
                        bindWithClass(processorMapBinder, key, loadedClass);
                    } catch (ClassNotFoundException e) {
                        LOGGER.error("Can't find class +" + value, e);
                        throw new IllegalArgumentException("Can't find class " + value);
                    }
                } else if (value instanceof Class) {
                    bindWithClass(processorMapBinder, key, (Class) value);
                } else if (value instanceof IKafkaMsgProcessor) {
                    requestInjection(value);
                    processorMapBinder.addBinding(key).toInstance((IKafkaMsgProcessor) value);
                } else if (value instanceof Provider) {
                    Provider msgProcessorProvider = (Provider) value;
                    requestInjection(msgProcessorProvider);
                    processorMapBinder.addBinding(key).toProvider(msgProcessorProvider);
                } else {
                    throw new RuntimeException("Cant't recognize the config key:" + key + ",value:" + value);
                }
            }
        }

        /**
         * class中加载IKafkaMsgProcessor
         * @param newMapBinder
         * @param key
         * @param loadedClass
         */
        private void bindWithClass(MapBinder<String, IKafkaMsgProcessor> newMapBinder, String key, Class loadedClass) {
            if (Provider.class.isAssignableFrom(loadedClass)) {
                Class<? extends Provider<? extends IKafkaMsgProcessor>> providerClass = (Class<? extends Provider<? extends IKafkaMsgProcessor>>) loadedClass;
                newMapBinder.addBinding(key).toProvider(providerClass).asEagerSingleton();
            } else if (IKafkaMsgProcessor.class.isAssignableFrom(loadedClass)) {
                Class<? extends IKafkaMsgProcessor> processorClass = loadedClass;
                newMapBinder.addBinding(key).to(processorClass).asEagerSingleton();
            } else {
                throw new IllegalArgumentException("Unsupported IKakfakMsgProcessor class:" + loadedClass);
            }
        }
    }
}

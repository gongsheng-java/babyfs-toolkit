package com.babyfs.tk.service.basic.mq.kafka.guice;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.io.Resources;
import com.google.inject.AbstractModule;
import com.babyfs.tk.service.basic.guice.SimpleBasicServiceModule;
import com.babyfs.tk.service.basic.mq.kafka.IKafkaProducer;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Kafka消息队列服务Moduel,尝试创建两个命名的{@link IKafkaProducer}:
 * <ul>
 * <li>name: {@value #ASYNC_PRODUCER} 使用{@link #confOfAsyncProducer}作为配置文件,如果配置文件存在就创建它</li>
 * <li>name: {@value #SYNC_PRODUCER} 使用{@link #confOfSyncProducer} 作为配置文件,如果配置文件存在就创建它</li>
 * </ul>
 */
public class KafkaProducerModule extends AbstractModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaProducerModule.class);
    /**
     * 异步发送的消息队列配置文件
     */
    public static final String CONF_KAFKA_ASYNC_PRODUCER = "kafka-producer-async.xml";
    /**
     * 同步发送的消息队列配置文件
     */
    public static final String CONF_KAFAK_SYNC_PRODUCER = "kafka-producer-sync.xml";

    public static final String CONF_KAFKA_USER_DATACENTER_PRODUCER = "user-data-center.xml";

    /**
     * 异步Producer的名称
     */
    public static final String ASYNC_PRODUCER = "async_producer";

    /**
     * 同步Producer的名称
     */
    public static final String SYNC_PRODUCER = "sync_producer";

    public static final String USER_DATACENTER_PRODUCER = "user_datacenter_producer";

    private final String confOfAsyncProducer;
    private final String confOfSyncProducer;
    private final String asyncProducerName;
    private final String syncProducerName;

    /**
     * 使用{@link #CONF_KAFKA_ASYNC_PRODUCER} 和 {@link #CONF_KAFAK_SYNC_PRODUCER}构建Module
     */
    public KafkaProducerModule() {
        this(CONF_KAFKA_ASYNC_PRODUCER, CONF_KAFAK_SYNC_PRODUCER);
    }

    /**
     * @param confOfAsyncProducer 异步发送Producer的配置文件
     * @param confOfSyncProducer  同步发送Producer的配置文件
     */
    public KafkaProducerModule(String confOfAsyncProducer, String confOfSyncProducer) {
        this(confOfAsyncProducer, ASYNC_PRODUCER, confOfSyncProducer, SYNC_PRODUCER);
    }

    /**
     * @param confOfAsyncProducer 异步发送Producer的配置文件
     * @param asyncProducerName   异步发送Producer的名称
     * @param confOfSyncProducer  同步发送Producer的配置文件
     * @param syncProducerName    同步发送Producer的名称
     */
    public KafkaProducerModule(String confOfAsyncProducer, String asyncProducerName, String confOfSyncProducer, String syncProducerName) {
        this.confOfAsyncProducer = StringUtils.trimToNull(confOfAsyncProducer);
        this.confOfSyncProducer = StringUtils.trimToNull(confOfSyncProducer);
        this.asyncProducerName = StringUtils.trimToNull(asyncProducerName);
        this.syncProducerName = StringUtils.trimToNull(syncProducerName);
        Preconditions.checkArgument(confOfSyncProducer != null || confOfAsyncProducer != null, "Can't find available kafka producer config.");
    }

    /**
     * @throws IllegalArgumentException 如果没有有效的配置,会抛出这个异常
     */
    @Override
    protected void configure() {
        boolean hasAsyncProducer = installProducerModule(this.confOfAsyncProducer, this.asyncProducerName);
        boolean hasSyncProducer = installProducerModule(this.confOfSyncProducer, this.syncProducerName);
        try{
            boolean hasUserDataCenterProducer = installProducerModule(CONF_KAFKA_USER_DATACENTER_PRODUCER, USER_DATACENTER_PRODUCER);
        }catch (Exception e){
            LOGGER.warn("error when build user datacenter: {}", e.getMessage());
        }
        if (!(hasAsyncProducer || hasSyncProducer)) {
            throw new IllegalArgumentException("Can't find async or sync producer config,please check the config file name for async:" + this.confOfAsyncProducer + ",sync:" + this.confOfSyncProducer);
        }
    }

    /**
     * 安装Producer Module
     *
     * @param configName
     * @param serviceName
     */
    protected boolean installProducerModule(String configName, String serviceName) {
        if (isResourceExist(configName)) {
            LOGGER.info("Install Kafka producer name {} from {}", serviceName, configName);
            SimpleBasicServiceModule<IKafkaProducer> module = KafkaModuleFactory.createProducerModule(configName, serviceName);
            install(module);
            return true;
        }
        return false;
    }

    /**
     * 检查指定的resource是否存在
     *
     * @param resource
     * @return
     * @throws IllegalArgumentException 如果resouce不为空,但是不存在时会抛出这个异常
     */
    protected static boolean isResourceExist(String resource) {
        if (Strings.isNullOrEmpty(resource)) {
            return false;
        }
        return Resources.getResource(resource) != null;
    }
}

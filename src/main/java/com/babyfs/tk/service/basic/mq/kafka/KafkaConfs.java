package com.babyfs.tk.service.basic.mq.kafka;

import java.util.Properties;

/**
 * Kafka配置
 */
public final class KafkaConfs {
    public static final String MAX_BLOCK_MS = "max.block.ms";
    /**
     * {@link #MAX_BLOCK_MS}的默认值:5秒
     */
    public static final String MAX_BLOCK_MS_DEFAULT = "5000";
    public static final String REQUEST_TIMEOUT_MS = "request.timeout.ms";
    /**
     * {@link #REQUEST_TIMEOUT_MS}的默认值:5秒
     */
    public static final String REQUEST_TIMEOUT_MS_DEFAULT = "10500";

    private KafkaConfs() {

    }

    /**
     * 设置默认参数:
     * {@value #MAX_BLOCK_MS}
     * {@value #REQUEST_TIMEOUT_MS}
     *
     * @param config
     */
    public static void setDefaultConfs(Properties config) {
        if (!config.containsKey(KafkaConfs.MAX_BLOCK_MS)) {
            config.setProperty(KafkaConfs.MAX_BLOCK_MS, KafkaConfs.MAX_BLOCK_MS_DEFAULT);
        }

        if (!config.containsKey(KafkaConfs.REQUEST_TIMEOUT_MS)) {
            config.setProperty(KafkaConfs.REQUEST_TIMEOUT_MS, KafkaConfs.REQUEST_TIMEOUT_MS_DEFAULT);
        }
    }
}

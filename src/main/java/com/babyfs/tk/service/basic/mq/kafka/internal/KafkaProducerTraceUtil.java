package com.babyfs.tk.service.basic.mq.kafka.internal;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Kafka Producer 日志工具,使用SLF4J记录日志:
 * <ul>
 * <li>
 * logger name: {@value #KAFKA_PRODUCER_ERROR}
 * </li>
 * <li>
 * logger level: TRACE
 * </li>
 * </ul>
 */
public final class KafkaProducerTraceUtil {
    public static final String KAFKA_PRODUCER_ERROR = "kafka.producer.error";
    private static final Logger LOGGER = LoggerFactory.getLogger(KAFKA_PRODUCER_ERROR);

    static {
        if (!LOGGER.isTraceEnabled()) {
            String msg = "The logger level of " + LOGGER.getName() + " must be trace enabled.";
            System.err.println(msg);
            throw new IllegalArgumentException(msg);
        }
    }

    private KafkaProducerTraceUtil() {

    }

    public static void init() {
        //Noting to do
    }

    /**
     * 将发送失败的消息记录到日志中,数据将被转为json记录在日志中
     *
     * @param topic Kafka Topic name
     * @param key
     * @param data  发送的数据
     */
    public static void logSendFail(String topic, Object key, Object data) {
        Preconditions.checkArgument(topic != null);
        if (data == null) {
            return;
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("topic", topic);
        jsonObject.put("key", key);
        jsonObject.put("data", data);
        LOGGER.trace("{}", jsonObject.toJSONString());
    }
}

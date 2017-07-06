package com.babyfs.tk.service.basic.mq.kafka;

import org.junit.Test;

/**
 *
 */
public class KafkaUtilTest {
    @Test
    public void groupConsumerOffset() throws Exception {
        int part = KafkaUtil.groupConsumerOffsetPart("prod-qiniu-callback", 50);
        System.out.println(part);
    }
}
package com.babyfs.tk.service.biz.base.model;

import org.junit.Test;

/**
 *
 */
public class MessageTypeTest {
    @Test
    public void test() {
        System.out.println(MessageType.UNKOWN.getIndex());
        System.out.println(MessageType.LOCAL_CACHE_CHANGE.getIndex());
    }
}
package com.babyfs.tk.commons.zookeeper;

import com.babyfs.tk.commons.utils.FunctionUtil;
import org.junit.Assert;

import java.io.IOException;
import java.util.Date;

/**
 */
public class ZkMapSetterTest {
    static ZkClient zkClient = new ZkClient("127.0.0.1:2181", "gsns", "gsns@zookeeper");

    public static void main(String[] args) throws InterruptedException {
        //test_add();
        test_recover();
    }

    public static void test_add() throws InterruptedException {
        FunctionUtil.StringToByteArray stringToByteArray = new FunctionUtil.StringToByteArray();
        FunctionUtil.ByteArrayToString byteArrayToString = new FunctionUtil.ByteArrayToString();
        ZkMapSetter<String> setter = new ZkMapSetter<String>(zkClient, "/test", stringToByteArray, true);
        String time = new Date().toString();
        for (int i = 0; i < 100; i++) {
            Assert.assertTrue(setter.put("key_" + i, "value_" + i + "_" + time));
        }
        ZkMap<String> getter = ZkMap.createRestrictZkMap(zkClient, "/test", byteArrayToString);
        for (int i = 0; i < 100; i++) {
            String s = getter.get("key_" + i);
            System.out.println(s);
            Assert.assertEquals("value_" + i + "_" + time, s);
        }
        for (int i = 0; i < 10; i++) {
            Assert.assertTrue(setter.remove("key_" + i));
        }
        Thread.sleep(1000);
        for (int i = 0; i < 10; i++) {
            String s = getter.get("key_" + i);
            Assert.assertNull(s);
        }
        for (int i = 10; i < 100; i++) {
            Assert.assertTrue(setter.remove("key_" + i));
        }
        for (int i = 10; i < 100; i++) {
            String s = getter.get("key_" + i);
            Assert.assertNull(s);
        }
        Assert.assertTrue(getter.isEmpty());
    }

    public static void test_recover() throws InterruptedException {
        FunctionUtil.StringToByteArray stringToByteArray = new FunctionUtil.StringToByteArray();
        FunctionUtil.ByteArrayToString byteArrayToString = new FunctionUtil.ByteArrayToString();
        //非持久化的map
        ZkMapSetter<String> setter = new ZkMapSetter<String>(zkClient, "/test", stringToByteArray, false);
        String time = new Date().toString();
        for (int i = 0; i < 100; i++) {
            Assert.assertTrue(setter.put("key_" + i, "value_" + i + "_" + time));
        }
        ZkMap<String> getter = ZkMap.createRestrictZkMap(zkClient, "/test", byteArrayToString);
        for (int i = 0; i < 100; i++) {
            String s = getter.get("key_" + i);
            System.out.println(s);
            Assert.assertEquals("value_" + i + "_" + time, s);
        }
        System.out.println("Sleep,shutdown zookeeper server and then press any key to continue.");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < 10; i++) {
            Assert.assertTrue(setter.remove("key_" + i));
        }
        Thread.sleep(1000);
        for (int i = 0; i < 10; i++) {
            String s = getter.get("key_" + i);
            Assert.assertNull(s);
        }
        for (int i = 10; i < 100; i++) {
            Assert.assertTrue(setter.remove("key_" + i));
        }
        while (true) {
            Thread.sleep(500);
            Assert.assertTrue(getter.isEmpty());
            System.out.println(getter.keySet());
        }
    }

}

package com.babyfs.tk.commons.utils;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 */
public class EncryptorTest {
    private String passPhrase = "b";

    private int thread = 4;
    private ExecutorService executorService = Executors.newFixedThreadPool(thread);

    @Test
    public void testDes() throws Exception {
        final DESEncryptor encrypterUtil = new DESEncryptor(passPhrase);
        testDecrypt(encrypterUtil, executorService, thread);
        long st = System.currentTimeMillis();
        testDecrypt(encrypterUtil, executorService, thread);
        long et = System.currentTimeMillis();
        System.out.println("des time:" + (et - st) + " ms");
    }

    //因为默认的JDK JCE Policy的限制,这个测试不默认启动
    @Test
    @Ignore
    public void testTripleDes() throws Exception {
        final DESEncryptor encrypterUtil = new DESEncryptor(passPhrase, DESEncryptor.PBE_WITH_MD5_AND_TRIPLE_DES);
        testDecrypt(encrypterUtil, executorService, thread);
        long st = System.currentTimeMillis();
        testDecrypt(encrypterUtil, executorService, thread);
        long et = System.currentTimeMillis();
        System.out.println("triple des time:" + (et - st) + " ms");
    }

    @Test
    public void testAes() throws Exception {
        AESEncryptor aesEncryptor = new AESEncryptor(passPhrase);
        testDecrypt(aesEncryptor, executorService, thread);
        long st = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            testDecrypt(aesEncryptor, executorService, thread);
        }
        long et = System.currentTimeMillis();
        System.out.println("ase time:" + (et - st) + " ms");
    }

    private void testDecrypt(final Encryptor encryptor, ExecutorService executorService, int thread) throws Exception {
        final int batchsize = 10;
        final CountDownLatch countDownLatch = new CountDownLatch(thread * batchsize);
        final String content0 = "中文,adsafdweraafdsf";
        final String content1 = "中文@adsafdweraafdsf";
        final AtomicInteger fail = new AtomicInteger();
        Runnable run = () -> {
            for (int i = 0; i < batchsize; i++) {
                try {
                    String e0 = encryptor.encrypt(content0);
                    String e1 = encryptor.encrypt(content1);
                    if (!Objects.equals(content0, encryptor.decrypt(e0))) {
                        fail.incrementAndGet();
                    }
                    if (!Objects.equals(content1, encryptor.decrypt(e1))) {
                        fail.incrementAndGet();
                    }
                } catch (Exception e) {
                    fail.incrementAndGet();
                    throw new RuntimeException(e);
                } finally {
                    countDownLatch.countDown();
                }
            }
        };
        for (int i = 0; i < thread; i++) {
            executorService.submit(run);
        }
        boolean b = countDownLatch.await(5, TimeUnit.SECONDS);
        Assert.assertTrue(b);
        Assert.assertEquals(0, fail.get());
    }
}

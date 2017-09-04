package com.babyfs.tk.commons.utils;

import org.apache.zookeeper.txn.Txn;
import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

/**
 *
 */
public class AESEncryptorTest {
    @Test
    public void encrypt() throws Exception {
        AESEncryptor aesEncryptor = new AESEncryptor(UUID.randomUUID().toString());
        String text = "123中国 abc";

        String encrypt = aesEncryptor.encrypt(text);
        System.out.println(encrypt);
        String decrypt = aesEncryptor.decrypt(encrypt);
        Assert.assertEquals(text, decrypt);

        encrypt = aesEncryptor.encrypt(text, true);
        System.out.println(encrypt);
        decrypt = aesEncryptor.decrypt(encrypt);
        Assert.assertEquals(text, decrypt);
    }
}
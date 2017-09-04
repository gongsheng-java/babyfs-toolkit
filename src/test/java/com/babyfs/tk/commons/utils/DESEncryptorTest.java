package com.babyfs.tk.commons.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

/**
 *
 */
public class DESEncryptorTest {
    @Test
    public void encrypt() throws Exception {
        DESEncryptor encryptor = new DESEncryptor(UUID.randomUUID().toString());
        String text = "123中国 abc";

        String encrypt = encryptor.encrypt(text);
        String decrypt = encryptor.decrypt(encrypt);
        Assert.assertEquals(text, decrypt);

        encrypt = encryptor.encrypt(text, true);
        decrypt = encryptor.decrypt(encrypt);
        Assert.assertEquals(text, decrypt);
    }
}
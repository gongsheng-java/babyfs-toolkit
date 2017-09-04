package com.babyfs.tk.service.basic.security.internal;

import com.babyfs.tk.commons.utils.AESEncryptor;
import com.babyfs.tk.service.basic.security.PasswordSaltHash;
import com.babyfs.tk.service.basic.security.PasswordSaltMD5Hash;
import org.apache.zookeeper.txn.Txn;
import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

/**
 *
 */
public class CryptoServiceImplTest {
    @Test
    public void encrypt() throws Exception {
        CryptoServiceImpl cryptoService = new CryptoServiceImpl(new PasswordSaltMD5Hash(), new AESEncryptor(UUID.randomUUID().toString()));
        String text = "abc 134 中国";

        String encrypt = cryptoService.encrypt(text);
        String decrypt = cryptoService.decrypt(encrypt);

        System.out.println(encrypt);
        Assert.assertEquals(text, decrypt);

        encrypt = cryptoService.encryptUrlSafe(text);
        decrypt = cryptoService.decrypt(encrypt);

        System.out.println(encrypt);
        Assert.assertEquals(text, decrypt);
    }
}
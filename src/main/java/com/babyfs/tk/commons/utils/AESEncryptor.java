package com.babyfs.tk.commons.utils;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.spec.KeySpec;

/**
 * 基于AES的加密解密器
 */
public class AESEncryptor implements Encryptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(AESEncryptor.class);
    private static final int ITERATIONS = 10;
    private static final String ALGORITHM = "AES";
    private static final String SECRET_KEY_ALGORITHM = "PBKDF2WithHmacSHA1";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final byte[] DEFAULT_SALT = {(byte) 0xD9, (byte) 0xAB, (byte) 0xD8, (byte) 0x42, (byte) 0x66, (byte) 0x45, (byte) 0xF3, (byte) 0x13};
    private static final byte[] DEFAULT_IV = {(byte) 0xB9, (byte) 0xA0, (byte) 0xE8, (byte) 0x62, (byte) 0x96, (byte) 0xA5, (byte) 0x13, (byte) 0xF3};
    public static final int AES_128 = 16 * 8;
    public static final int AES_192 = 24 * 8;
    public static final int AES_256 = 32 * 8;
    public static final int DEFAULT_AES_LENGTH = AES_128;

    /**
     * 生成的秘钥
     */
    private final SecretKey secretKey;
    /**
     * 初始化向量
     */
    private final IvParameterSpec iv;

    /**
     * 使用默认的salt{@link #DEFAULT_SALT}和默认的AES KEY 长度{@value #DEFAULT_AES_LENGTH}构建AES加密实例
     *
     * @param passPhrase 密码串
     */
    public AESEncryptor(String passPhrase) {
        this(passPhrase, DEFAULT_SALT, DEFAULT_AES_LENGTH);
    }

    /**
     * 使用默认的salt{@link #DEFAULT_SALT}和指定长度的AES KEY长度构建AES加密实例
     *
     * @param passPhrase
     * @param aesKeyLength
     */
    public AESEncryptor(String passPhrase, int aesKeyLength) {
        this(passPhrase, DEFAULT_SALT, aesKeyLength);
    }

    /**
     * @param passPhrase   密码串
     * @param salt         指定的干扰盐
     * @param aesKeyLength AES密钥的长度
     */
    public AESEncryptor(String passPhrase, byte[] salt, int aesKeyLength) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(passPhrase));
        Preconditions.checkArgument(salt != null);
        checkAESKeyLength(aesKeyLength);
        this.secretKey = genKey(passPhrase, salt, aesKeyLength);
        byte[] ivb = new byte[16];
        for (int i = 0; i < ivb.length; i++) {
            ivb[i] = DEFAULT_IV[i % DEFAULT_IV.length];
        }
        iv = new IvParameterSpec(ivb);
    }


    @Override
    public String encrypt(String str) {
        return this.encrypt(str, false);
    }

    @Override
    public String encrypt(String str, boolean urlSafe) {
        try {
            SecretKeySpec key = cloneSecretKeySpec();
            Cipher ecipher = Cipher.getInstance(TRANSFORMATION);
            ecipher.init(Cipher.ENCRYPT_MODE, key, iv);
            return urlSafe ? EncryptorUtil.encodeBase64ByCipherURLSafe(str, ecipher) : EncryptorUtil.encodeBase64ByCipher(str, ecipher);
        } catch (Exception e) {
            LOGGER.error("encrypt error: str[" + str + "]  ", e);
        }
        return null;
    }


    @Override
    public String decrypt(String str) {
        try {
            SecretKeySpec key = cloneSecretKeySpec();
            Cipher dcipher = Cipher.getInstance(TRANSFORMATION);
            dcipher.init(Cipher.DECRYPT_MODE, key, iv);
            return EncryptorUtil.decodeBase64ByCipher(str, dcipher);
        } catch (Exception e) {
            LOGGER.error("decrypt error: str[" + str + "]  ", e);
        }
        return null;
    }


    /**
     * @param passPhrase
     * @param salt
     * @return
     */
    private SecretKey genKey(String passPhrase, byte[] salt, int aesKeyLength) {
        try {
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(SECRET_KEY_ALGORITHM);
            KeySpec spec = new PBEKeySpec(passPhrase.toCharArray(), salt, ITERATIONS, aesKeyLength);
            SecretKey tmp = secretKeyFactory.generateSecret(spec);
            return new SecretKeySpec(tmp.getEncoded(), ALGORITHM);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 由于不清楚this.secretKey是否线程安全,这里clone一个出来
     *
     * @return
     */
    private SecretKeySpec cloneSecretKeySpec() {
        return new SecretKeySpec(this.secretKey.getEncoded(), this.secretKey.getAlgorithm());
    }

    private void checkAESKeyLength(int size) {
        if (size != AES_128 && size != AES_192 && size != AES_256) {
            throw new IllegalArgumentException("Invalid AES Key length:" + size + ",Only " + AES_128 + "," + AES_192 + "," + AES_256);
        }
    }
}

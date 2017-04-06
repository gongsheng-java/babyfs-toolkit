package com.babyfs.tk.commons.utils;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.spec.KeySpec;
import java.util.Arrays;

/**
 * 基于DES的加密解密器
 */
public class DESEncryptor implements Encryptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DESEncryptor.class);

    private static final byte[] DEFAULT_SALT = {(byte) 0xB9, (byte) 0xAB, (byte) 0xD8, (byte) 0x42, (byte) 0x66, (byte) 0x45, (byte) 0xF3, (byte) 0x13};
    /**
     * 56 bit key,DES 加密算法
     */
    public static final String PBE_WITH_MD5_AND_DES = "PBEWithMD5AndDES";
    /**
     * 168 bit key,Triple DES 加密算法
     */
    public static final String PBE_WITH_MD5_AND_TRIPLE_DES = "PBEWithMD5AndTripleDES";
    /**
     * 迭代次數
     */
    private static final int ITERATION_COUNT = 17;
    /**
     * 加密算法
     */
    private final String alogrithName;
    private final byte[] salt;
    private final SecretKey secretKey;

    /**
     * 使用默认的slat {@link #DEFAULT_SALT} 和{@link #PBE_WITH_MD5_AND_DES}构建
     *
     * @param passPhrase 密码
     * @see {@link #DESEncryptor(String, String)}
     */
    public DESEncryptor(String passPhrase) {
        this(passPhrase, PBE_WITH_MD5_AND_DES);
    }

    /**
     * 使用指定的slat 和 {@link #PBE_WITH_MD5_AND_DES} 构建
     *
     * @param passPhrase
     * @param newSalt
     */
    public DESEncryptor(String passPhrase, byte[] newSalt) {
        this(passPhrase, PBE_WITH_MD5_AND_DES, newSalt);
    }

    /**
     * @param passPhrase   密码
     * @param alogrithName 加密算法
     */
    public DESEncryptor(String passPhrase, String alogrithName) {
        this(passPhrase, alogrithName, DEFAULT_SALT);
    }


    /**
     * @param passPhrase   密码
     * @param algorithName 加密算法,可选的有{@link #PBE_WITH_MD5_AND_TRIPLE_DES},{@link #PBE_WITH_MD5_AND_DES}
     * @param newSalt      干扰盐
     */
    public DESEncryptor(String passPhrase, String algorithName, byte[] newSalt) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(passPhrase));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(algorithName));
        Preconditions.checkArgument(newSalt != null);
        this.salt = Arrays.copyOf(newSalt, newSalt.length);
        this.alogrithName = algorithName;
        this.secretKey = genKey(passPhrase.toCharArray(), this.salt, ITERATION_COUNT);
    }


    @Override
    public String encrypt(String str) {
        try {
            SecretKeySpec key = cloneSecretKeySpec();
            Cipher ecipher = Cipher.getInstance(key.getAlgorithm());
            PBEParameterSpec pbeParameterSpec = new PBEParameterSpec(this.salt, ITERATION_COUNT);
            ecipher.init(Cipher.ENCRYPT_MODE, key, pbeParameterSpec);
            return EncryptorUtil.encodeBase64ByCipher(str, ecipher);
        } catch (Exception e) {
            LOGGER.error("encrypt error: str[" + str + "]  ", e);
        }
        return null;
    }


    @Override
    public String decrypt(String str) {
        try {
            SecretKeySpec key = cloneSecretKeySpec();
            Cipher dcipher = Cipher.getInstance(key.getAlgorithm());
            PBEParameterSpec pbeParameterSpec = new PBEParameterSpec(this.salt, ITERATION_COUNT);
            dcipher.init(Cipher.DECRYPT_MODE, key, pbeParameterSpec);
            return EncryptorUtil.decodeBase64ByCipher(str, dcipher);
        } catch (Exception e) {
            LOGGER.error("decrypt error: str[" + str + "]  ", e);
        }

        return null;
    }

    /**
     * 由于不清楚this.secretKey是否线程安全,这里clone一个出来
     *
     * @return
     */
    private SecretKeySpec cloneSecretKeySpec() {
        return new SecretKeySpec(this.secretKey.getEncoded(), this.secretKey.getAlgorithm());
    }

    private SecretKey genKey(char[] passPhrase, byte[] salt, int iterationCount) {
        KeySpec keySpec = new PBEKeySpec(passPhrase, salt, iterationCount);
        try {
            return SecretKeyFactory.getInstance(alogrithName).generateSecret(keySpec);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

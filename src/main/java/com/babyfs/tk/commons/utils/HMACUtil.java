package com.babyfs.tk.commons.utils;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * HMAC工具类
 * <p/>
 */
public final class HMACUtil {

    private static final String HMAC_MD5 = "HmacMD5";

    private HMACUtil() {
    }

    /**
     * 使用md5做为摘要算法的hmac
     *
     * @param key  密钥
     * @param data 数据
     * @return 消息认证码
     */
    public static byte[] md5HMAC(byte[] key, byte[] data) {
        try {
            Mac mac = Mac.getInstance(HMAC_MD5);
            SecretKey secretKey = new SecretKeySpec(key, HMAC_MD5);
            mac.init(secretKey);
            return mac.doFinal(data);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
}

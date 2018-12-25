package com.babyfs.tk.commons.utils;

import com.babyfs.tk.commons.Constants;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;

/**
 * 工具类
 */
public class EncryptorUtil {
    private EncryptorUtil() {

    }

    /**
     * 使用cipher对str进行编码,返回编码后的Base64编码
     *
     * @param str
     * @param cipher
     * @return
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public static String encodeBase64ByCipher(String str, Cipher cipher) throws IllegalBlockSizeException, BadPaddingException {
        byte[] utf8 = str.getBytes(Constants.UTF8_CHARSET);
        byte[] enc = cipher.doFinal(utf8);
        return Base64.encodeBase64String(enc);
    }

    /**
     * 使用cipher对base64字符串进行编码,返回编码后的字符串
     *
     * @param base64
     * @param cipher
     * @return
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws UnsupportedEncodingException
     */
    public static String decodeBase64ByCipher(String base64, Cipher cipher) throws IllegalBlockSizeException, BadPaddingException {
        byte[] dec = Base64.decodeBase64(base64);
        byte[] utf8 = cipher.doFinal(dec);
        return new String(utf8, Constants.UTF8_CHARSET);
    }

    /**
     * 使用cipher对str进行编码,返回编码后的URL安全的Base64编码
     *
     * @param str
     * @param cipher
     * @return
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public static String encodeBase64ByCipherURLSafe(String str, Cipher cipher) throws IllegalBlockSizeException, BadPaddingException {
        byte[] utf8 = str.getBytes(Constants.UTF8_CHARSET);
        byte[] enc = cipher.doFinal(utf8);
        return Base64.encodeBase64URLSafeString(enc);
    }

    /**
     * 功能:AES加密
     *
     * @param content  需要加密的内容
     * @param password 加密密码
     * @return
     */

    public static String aesEncrypt(String content, String password) {
        try {
            SecretKeySpec key = new SecretKeySpec(password.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");// 创建密码器
            cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化
            return EncryptorUtil.encodeBase64ByCipher(content, cipher);
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * 功能: AES解密
     *
     * @param content  待解密内容
     * @param password 解密密钥
     * @return
     */

    public static String aesDecrypt(String content, String password) {
        try {
            SecretKeySpec key = new SecretKeySpec(password.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");// 创建密码器
            cipher.init(Cipher.DECRYPT_MODE, key);// 初始化
            return EncryptorUtil.decodeBase64ByCipher(content, cipher);
        } catch (Exception e) {
            return null;
        }
    }
}

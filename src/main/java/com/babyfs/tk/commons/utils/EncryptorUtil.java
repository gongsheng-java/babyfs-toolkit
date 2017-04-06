package com.babyfs.tk.commons.utils;

import com.babyfs.tk.commons.Constants;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
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
        byte[] utf8 = str.getBytes(Constants.DEFAULT_CHARSET_OBJ);
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
        return new String(utf8, Constants.DEFAULT_CHARSET_OBJ);
    }
}

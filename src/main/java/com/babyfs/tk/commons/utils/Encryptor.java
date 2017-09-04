package com.babyfs.tk.commons.utils;

/**
 */
public interface Encryptor {
    /**
     * 对字符串进行加密处理,返回的结果是URL编码不安全的
     *
     * @param str
     * @return
     */
    String encrypt(String str);

    /**
     * 对字符串进行加密处理,返回的结果是URL编码安全的
     *
     * @param str
     * @param urlSafe 是否URL编码安全
     * @return
     */
    String encrypt(String str, boolean urlSafe);

    /**
     * 对字符串进行解密处理
     *
     * @param str
     * @return
     */
    String decrypt(String str);

}

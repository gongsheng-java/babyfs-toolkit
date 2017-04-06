package com.babyfs.tk.service.basic.security;

import com.babyfs.tk.commons.base.Pair;

import javax.annotation.Nullable;

/**
 * 提供加密和解密相关的服务
 */
public interface ICryptoService {
    /**
     * 对密码进行加盐并哈希
     * <p/>
     * 返回的结果为{@link Pair}:
     * {@link Pair#getFirst()}: 密码加盐并哈希后小写16进制表示的结果
     * {@link Pair#second}: 小写16进制表示的盐值
     *
     * @param password 密码,不能为空
     * @return 加盐并哈希后的结果
     * @throws IllegalArgumentException
     */
    Pair<String, String> createSaltHash(String password);


    /**
     * 验证密码和盐值
     *
     * @param toCheckPassword 待验证的密码,不能为空
     * @param hashedPassword  哈希过的密码,不能为空
     * @param hexSalt         与hashedPassword对应的小写16进制表示的盐值,由所使用的Hash算法决定是否可以为空
     * @return true, 验证通过;false,验证失败
     * @throws IllegalArgumentException
     */
    boolean validatePassword(String toCheckPassword, String hashedPassword, @Nullable String hexSalt);

    /**
     * 对字符串进行加密处理
     *
     * @param str
     * @return
     */
    String encrypt(String str);

    /**
     * 对字符串进行解密处理
     *
     * @param str
     * @return
     */
    String decrypt(String str);
}

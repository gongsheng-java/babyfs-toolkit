package com.babyfs.tk.service.basic.security;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.hash.HashCode;
import com.babyfs.tk.commons.base.Pair;

import javax.annotation.Nullable;
import java.security.SecureRandom;

/**
 * 对密码加盐,哈希的基类
 * Salt: 使用{@link SecureRandom}生成,slat的长度(字节)由{@link #saltLength}设置,默认长度{@value #DEFAULT_SALT_BYTES}
 * 哈希方式: 由子类确定
 */
public abstract class PasswordSaltHash {
    /**
     * 默认的SALT字节长度
     */
    public static final int DEFAULT_SALT_BYTES = 10;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * slat字节长度
     */
    private final int saltLength;

    public PasswordSaltHash() {
        this(DEFAULT_SALT_BYTES);
    }

    public PasswordSaltHash(int saltLength) {
        Preconditions.checkArgument(saltLength > 0, "The salt length must > 0");
        this.saltLength = saltLength;
    }


    /**
     * 对密码进行加盐并哈希,使用{@link SecureRandom}自动生成盐值
     * <p/>
     * 返回的结果为{@link Pair}:
     * {@link Pair#getFirst()}: 密码加盐并哈希后小写16进制表示的结果
     * {@link Pair#second}: 小写16进制表示的盐值
     *
     * @param password 密码,不能为空
     * @return 加盐并哈希后的结果
     * @throws IllegalArgumentException
     */
    public Pair<String, String> createHash(String password) {
        checkPassword(password);
        byte[] saltBytes = new byte[this.saltLength];
        SECURE_RANDOM.nextBytes(saltBytes);
        return hashPassAndSalt(password, saltBytes);
    }


    /**
     * 对密码使用指定的盐值加盐并哈希
     *
     * @param password 密码,不能为空
     * @param hexSalt  指定的小写16进制表示的盐值,可以为空(表示不加盐)
     * @return
     * @throws IllegalArgumentException
     */
    public Pair<String, String> createHash(String password, @Nullable String hexSalt) {
        checkPassword(password);
        byte[] saltBytes = null;
        if (hexSalt != null && !hexSalt.isEmpty()) {
            saltBytes = HashCode.fromString(hexSalt).asBytes();
        }
        return hashPassAndSalt(password, saltBytes);
    }


    /**
     * 验证密码和盐值
     *
     * @param toCheckPassword 待验证的密码,不能为空
     * @param hashedPassword  哈希过的密码,不能为空
     * @param hexSalt         与<code>hashedPassword</code>对应的小写16进制表示的盐值,可以为空
     * @return true, 验证通过;false,验证失败
     * @throws IllegalArgumentException
     */
    public boolean validatePassword(String toCheckPassword, String hashedPassword, @Nullable String hexSalt) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(toCheckPassword), "The toCheckPassword not be null or empty");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(hashedPassword), "The hashedPassword not be null or empty");
        byte[] saltBytes = null;
        if (hexSalt != null && !hexSalt.isEmpty()) {
            saltBytes = HashCode.fromString(hexSalt).asBytes();
        }
        String passSaltHash = hash(toCheckPassword, saltBytes);
        return hashedPassword.equals(passSaltHash);
    }

    /**
     * 对密码使用任意的的盐值加盐并哈希
     *
     * @param password 密码,不能为空
     * @param salt     任意盐值,可以为空(表示不加盐)
     * @return
     * @throws IllegalArgumentException
     */
    public Pair<String, String> createHashWithArbSalt(String password, @Nullable String salt) {
        checkPassword(password);
        byte[] saltBytes = null;
        if (salt != null && !salt.isEmpty()) {
            saltBytes = salt.getBytes(Charsets.UTF_8);
        }
        return hashPassAndArbSalt(password, saltBytes);
    }

    /**
     * 验证密码和盐值
     *
     * @param toCheckPassword 待验证的密码,不能为空
     * @param hashedPassword  哈希过的密码,不能为空
     * @param salt            任意的盐值,可以为空
     * @return true, 验证通过;false,验证失败
     * @throws IllegalArgumentException
     */
    public boolean validatePasswordWithArbSalt(String toCheckPassword, String hashedPassword, @Nullable String salt) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(toCheckPassword), "The toCheckPassword not be null or empty");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(hashedPassword), "The hashedPassword not be null or empty");
        byte[] saltBytes = null;
        if (salt != null && !salt.isEmpty()) {
            saltBytes = salt.getBytes(Charsets.UTF_8);
        }
        String passSaltHash = hash(toCheckPassword, saltBytes);
        return hashedPassword.equals(passSaltHash);
    }

    /**
     * 对password和saltBytes进行哈希,具体的哈希方式由子类决定
     *
     * @return
     * @throws IllegalArgumentException
     */
    protected abstract String hash(String password, byte[] saltBytes);

    /**
     * 16进制加盐
     *
     * @param password
     * @param saltBytes
     * @return
     */
    private Pair<String, String> hashPassAndSalt(String password, byte[] saltBytes) {
        String passSaltHash = hash(password, saltBytes);
        if (saltBytes != null) {
            return Pair.of(passSaltHash, HashCode.fromBytes(saltBytes).toString());
        } else {
            return Pair.of(passSaltHash, null);
        }
    }

    /**
     * 任意盐值加盐
     *
     * @param password
     * @param arbSaltBytes
     * @return
     */
    private Pair<String, String> hashPassAndArbSalt(String password, byte[] arbSaltBytes) {
        String passSaltHash = hash(password, arbSaltBytes);
        if (arbSaltBytes != null) {
            return Pair.of(passSaltHash, new String(arbSaltBytes, Charsets.UTF_8));
        } else {
            return Pair.of(passSaltHash, null);
        }
    }

    private void checkPassword(String password) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(password), "The password must not be null or empty");
    }
}

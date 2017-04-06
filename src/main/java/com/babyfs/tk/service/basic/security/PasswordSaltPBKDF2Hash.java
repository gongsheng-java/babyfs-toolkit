package com.babyfs.tk.service.basic.security;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.hash.HashCode;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

/**
 * 使用PBKDF2实现的加密哈希,需要提供非空的saltBytes作为key
 * PBKDF2哈希后的结果为160位,由长度40的16进制字符串表示
 */
public class PasswordSaltPBKDF2Hash extends PasswordSaltHash {
    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA1";
    private static final int SHA1_KEY_BITS = 160;
    private static final int PBKDF2_DEFAULT_ITERATIONS = 10;

    private final int iterations;

    public PasswordSaltPBKDF2Hash() {
        this(PBKDF2_DEFAULT_ITERATIONS);
    }

    public PasswordSaltPBKDF2Hash(int iterations) {
        Preconditions.checkArgument(iterations > 0, "The iterations must >0");
        this.iterations = iterations;
    }

    /**
     * @param password  非空
     * @param saltBytes 非空
     * @return
     */
    @Override
    protected String hash(String password, byte[] saltBytes) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(password), "The password must not be null or empty");
        Preconditions.checkArgument(saltBytes != null && saltBytes.length > 0, "The saltBytes must not be empty");
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), saltBytes, this.iterations, SHA1_KEY_BITS);
        SecretKeyFactory skf;
        try {
            skf = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
            byte[] encoded = skf.generateSecret(spec).getEncoded();
            return HashCode.fromBytes(encoded).toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }
}

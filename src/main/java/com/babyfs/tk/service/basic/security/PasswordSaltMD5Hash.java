package com.babyfs.tk.service.basic.security;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.babyfs.tk.commons.Constants;

/**
 * 使用MD5进行哈希,MD5哈希后的结果为128位,由长度32的16进制字符串表示
 * <p>
 * hash的格式为:`password`+`_`+`salt`
 */
public class PasswordSaltMD5Hash extends PasswordSaltHash {
    /**
     * 密码与盐值中间的分隔符
     */
    private static final byte[] PASS_SALT_SEP = "_".getBytes(Constants.DEFAULT_CHARSET_OBJ);

    public PasswordSaltMD5Hash() {
    }

    public PasswordSaltMD5Hash(int saltLength) {
        super(saltLength);
    }

    /**
     * @return
     * @throws IllegalArgumentException
     */
    protected String hash(String password, byte[] saltBytes) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(password));
        HashFunction hashFunction = Hashing.md5();
        Hasher hasher = hashFunction.newHasher();
        hasher.putBytes(password.getBytes(Constants.DEFAULT_CHARSET_OBJ));
        if (saltBytes != null && saltBytes.length > 0) {
            hasher.putBytes(PASS_SALT_SEP);
            hasher.putBytes(saltBytes);
        }
        return hasher.hash().toString();
    }
}

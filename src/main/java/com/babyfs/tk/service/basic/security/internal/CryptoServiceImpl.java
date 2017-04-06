package com.babyfs.tk.service.basic.security.internal;

import com.google.common.base.Preconditions;
import com.babyfs.tk.commons.base.Pair;
import com.babyfs.tk.commons.utils.Encryptor;
import com.babyfs.tk.service.basic.security.ICryptoService;
import com.babyfs.tk.service.basic.security.PasswordSaltHash;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Crypto服务器的基础实现
 *
 * @see {@link PasswordSaltHash}
 * @see {@link Encryptor}
 */
public class CryptoServiceImpl implements ICryptoService {
    private final PasswordSaltHash passwordSaltHash;
    private final Encryptor encryptor;

    @Inject
    public CryptoServiceImpl(PasswordSaltHash passwordSaltHash, Encryptor encryptor) {
        Preconditions.checkArgument(passwordSaltHash != null, "passwordSaltHash must not be null");
        Preconditions.checkArgument(encryptor != null, "encryptor must not be null");
        this.passwordSaltHash = passwordSaltHash;
        this.encryptor = encryptor;
    }

    @Override
    public Pair<String, String> createSaltHash(String password) {
        return passwordSaltHash.createHash(password);
    }

    @Override
    public boolean validatePassword(String toCheckPassword, String hashedPassword, @Nullable String hexSalt) {
        return passwordSaltHash.validatePassword(toCheckPassword, hashedPassword, hexSalt);
    }

    @Override
    public String encrypt(String str) {
        return encryptor.encrypt(str);
    }

    @Override
    public String decrypt(String str) {
        return encryptor.decrypt(str);
    }
}

package com.babyfs.tk.service.basic.security.guice;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Provider;
import com.babyfs.tk.commons.utils.AESEncryptor;
import com.babyfs.tk.commons.utils.DESEncryptor;
import com.babyfs.tk.commons.utils.Encryptor;
import com.babyfs.tk.service.basic.guice.annotation.ServiceConf;
import com.babyfs.tk.service.basic.security.ICryptoService;
import com.babyfs.tk.service.basic.security.PasswordSaltHash;
import com.babyfs.tk.service.basic.security.PasswordSaltMD5Hash;
import com.babyfs.tk.service.basic.security.PasswordSaltPBKDF2Hash;
import com.babyfs.tk.service.basic.security.internal.CryptoServiceImpl;

import javax.inject.Inject;
import java.util.Map;

/**
 * {@link ICryptoService}实现的提供者,所需要的参数由{@link #conf}中的`crypto.*`指定
 * <p/>
 * {@link PasswordSaltHash} 支持下面的类型:
 * <ul>
 * <li>{@value #PASS_SALT_MD_5} </li>
 * <li>{@value #PASS_SALT_PBKDF_2}</li>
 * </ul>
 * <p/>
 * {@link Encryptor}支持下面的类型:
 * <ul>
 * <li>{@value #ENCRYPT_DES}</li>
 * <li>{@value #ENCRYPT_TRIPLE_DES}</li>
 * <li>{@value #ENCRYPT_AES}</li>
 * <li>{@value #ENCRYPT_AES_192}</li>
 * <li>{@value #ENCRYPT_AES_256}</li>
 * </ul>
 */
public class CryptoServiceProvider implements Provider<ICryptoService> {
    public static final String CRYPTO_PASSHASH_TYPE = "crypto.passhash.type";
    public static final String CRYPTO_ENCRYPTOR_TYPE = "crypto.encryptor.type";
    public static final String CRYPTO_ENCRYPTOR_PASS = "crypto.encryptor.pass";

    public static final String PASS_SALT_MD_5 = "MD5";
    public static final String PASS_SALT_PBKDF_2 = "PBKDF2";

    public static final String ENCRYPT_DES = "DES";
    public static final String ENCRYPT_TRIPLE_DES = "TripleDES";
    public static final String ENCRYPT_AES = "AES";
    public static final String ENCRYPT_AES_192 = "AES192";
    public static final String ENCRYPT_AES_256 = "AES256";
    public static final String ENCRYPT_AES_128 = "AES128";

    private final Map<String, String> conf;

    @Inject
    public CryptoServiceProvider(@ServiceConf Map<String, String> conf) {
        Preconditions.checkArgument(conf != null, "The config of CryptoService must not be null.");
        this.conf = conf;
    }

    @Override
    public ICryptoService get() {
        PasswordSaltHash passwordSaltHash = createPasswordSaltHash(this.conf);
        Encryptor encryptor = createEncryptor(this.conf);
        return new CryptoServiceImpl(passwordSaltHash, encryptor);
    }

    private PasswordSaltHash createPasswordSaltHash(Map<String, String> conf) {
        String type = conf.get(CRYPTO_PASSHASH_TYPE);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(type), "Can't find " + CRYPTO_PASSHASH_TYPE);
        if (type.equalsIgnoreCase(PASS_SALT_MD_5)) {
            return new PasswordSaltMD5Hash();
        } else if (type.equalsIgnoreCase(PASS_SALT_PBKDF_2)) {
            return new PasswordSaltPBKDF2Hash();
        } else {
            throw new IllegalArgumentException("Can't find the pass salt tyep:" + type);
        }
    }

    private Encryptor createEncryptor(Map<String, String> conf) {
        String type = conf.get(CRYPTO_ENCRYPTOR_TYPE);
        String pass = conf.get(CRYPTO_ENCRYPTOR_PASS);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(type), "Can't find " + CRYPTO_ENCRYPTOR_TYPE);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(pass), "Can't find " + CRYPTO_ENCRYPTOR_PASS);
        if (type.equalsIgnoreCase(ENCRYPT_DES)) {
            return new DESEncryptor(pass, DESEncryptor.PBE_WITH_MD5_AND_DES);
        } else if (type.equalsIgnoreCase(ENCRYPT_TRIPLE_DES)) {
            return new DESEncryptor(pass, DESEncryptor.PBE_WITH_MD5_AND_TRIPLE_DES);
        } else if (type.equalsIgnoreCase(ENCRYPT_AES) || type.equalsIgnoreCase(ENCRYPT_AES_128)) {
            return new AESEncryptor(pass, AESEncryptor.AES_128);
        } else if (type.equalsIgnoreCase(ENCRYPT_AES_192)) {
            return new AESEncryptor(pass, AESEncryptor.AES_192);
        } else if (type.equalsIgnoreCase(ENCRYPT_AES_256)) {
            return new AESEncryptor(pass, AESEncryptor.AES_256);
        } else {
            throw new IllegalArgumentException("Can't find the encryptor type:" + type);
        }
    }
}

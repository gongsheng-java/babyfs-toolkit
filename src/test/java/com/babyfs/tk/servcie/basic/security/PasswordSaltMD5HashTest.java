package com.babyfs.tk.servcie.basic.security;

import com.babyfs.tk.commons.base.Pair;
import com.babyfs.tk.service.basic.security.PasswordSaltMD5Hash;
import com.babyfs.tk.service.basic.security.PasswordSaltMD5NoSepHash;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class PasswordSaltMD5HashTest {
    @Test
    public void hash() throws Exception {
        PasswordSaltMD5Hash passwordSaltMD5Hash = new PasswordSaltMD5Hash(5);
        Pair<String, String> pair = passwordSaltMD5Hash.createHash("root");
        System.out.println(pair.first + " " + pair.second);
    }

    @Test
    public void arbHash() throws Exception {
        PasswordSaltMD5NoSepHash passwordSaltMD5Hash = new PasswordSaltMD5NoSepHash(5);
        String salt = "Pcmmr]g[*Z'v";
        Pair<String, String> hashWithArbSalt = passwordSaltMD5Hash.createHashWithArbSalt("1234567", salt);
        System.out.println(hashWithArbSalt);
        Assert.assertEquals("dcd4a9ca4d3d85e43b51e379c5d59461",hashWithArbSalt.getFirst());
        Assert.assertTrue(passwordSaltMD5Hash.validatePasswordWithArbSalt("1234567", hashWithArbSalt.first, salt));
        Assert.assertFalse(passwordSaltMD5Hash.validatePasswordWithArbSalt("1234568", hashWithArbSalt.first, salt));
         hashWithArbSalt = passwordSaltMD5Hash.createHashWithArbSalt("1", salt);
        System.out.println(hashWithArbSalt);
        Assert.assertEquals("87bb135cc9613e4ffa5064a9092c5b3f",hashWithArbSalt.getFirst());
    }

}
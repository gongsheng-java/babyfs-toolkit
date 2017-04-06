package com.babyfs.tk.commons.utils;

import org.apache.commons.codec.binary.Hex;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * {@link HMACUtil} 单元测试
 * <p/>
 * <a href="http://tools.ietf.org/html/rfc2104">HMAC: Keyed-Hashing for Message Authentication</a>
 * <p/>
 */
public class HMACUtilTest {
    @Test
    public void testMd5HMAC() throws Exception {
        byte[] key = "key".getBytes();
        byte[] data = "The quick brown fox jumps over the lazy dog".getBytes();
        byte[] mac = HMACUtil.md5HMAC(key, data);
        assertEquals("80070713463e7749b90c2dc24911e275", Hex.encodeHexString(mac));
    }

    @Test
    public void testMd5HMAC1() throws Exception {
        byte[] key = {0x0b, 0x0b, 0x0b, 0x0b, 0x0b, 0x0b, 0x0b, 0x0b, 0x0b, 0x0b, 0x0b, 0x0b, 0x0b, 0x0b, 0x0b, 0x0b};
        byte[] data = "Hi There".getBytes();
        byte[] mac = HMACUtil.md5HMAC(key, data);
        assertEquals("9294727a3638bb1c13f48ef8158bfc9d", Hex.encodeHexString(mac));
    }

    @Test
    public void testMd5HMAC2() throws Exception {
        byte[] key = "Jefe".getBytes();
        byte[] data = "what do ya want for nothing?".getBytes();
        byte[] mac = HMACUtil.md5HMAC(key, data);
        assertEquals("750c783e6ab0b503eaa86e310a5db738", Hex.encodeHexString(mac));
    }

}

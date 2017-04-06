package com.babyfs.tk.commons.utils;

import org.apache.commons.codec.binary.Base64;
import org.junit.Assert;
import org.junit.Test;

import java.security.Key;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

/**
 */
public class RSAEncrypterUtilTest {
    @Test
    public void testDecrypt() throws Exception {
        int thread = 2;
        final int batchsize = 10;
        final CountDownLatch countDownLatch = new CountDownLatch(thread * batchsize);
        ExecutorService executorService = Executors.newFixedThreadPool(thread);
        final Map keyPair = RSAEncrypterUtil.genKeyPair();
        final String data = "launchd|UserEventAgent|wifid|syslogd|powerd|launchd|UserEventAgent|wifid|syslogd|powerd|launchd|UserEventAgent|wifid|syslogd|powerd|launchd|UserEventAgent|wifid|syslogd|powerd|launchd|UserEventAgent|wifid|syslogd|powerd|launchd|UserEventAgent|wifid|syslogd|powerd|launchd|UserEventAgent|wifid|syslogd|powerd|launchd|UserEventAgent|wifid|syslogd|powerd|";
        final AtomicInteger fail = new AtomicInteger();
        Runnable run = () -> {
            for (int i = 0; i < batchsize; i++) {
                try {
                    byte[] encryptedData = RSAEncrypterUtil.encryptByPublicKey(data.getBytes(), (Key) keyPair.get(RSAEncrypterUtil.PUBLIC_KEY));
                    byte[] dencryptedData = RSAEncrypterUtil.decryptByPrivateKey(encryptedData, (Key) keyPair.get(RSAEncrypterUtil.PRIVATE_KEY));
                    if (!Objects.equals(data, new String(dencryptedData))) {
                        fail.incrementAndGet();
                    }
                    System.out.println(new String(dencryptedData));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    countDownLatch.countDown();
                }
            }
        };
        for (int i = 0; i < thread; i++) {
            executorService.submit(run);
        }
        countDownLatch.await();
        Assert.assertEquals(0, fail.get());
    }

    @Test
    public void getKeys() {
        String PRIVATEKEY = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAMvcmlJ60zSGuRqu146A8rFRYZFZrz1s8PXxiE6zBQmOWlxz4/f9o55Tth19HcQrxzL4sMvvFoeQ5fagQWTry+UM7pkZ4eyRoq+gymnZcSzJVWkIZA3UwpKajp6FCWaVCo42eKdtKNoyS7xANfPOJEgT/O3Po2bFs8e5hnMjiVTrAgMBAAECgYBLDBULlAe4aHgwKHlWDoZJXIosdfWbCUXTZG0ne6O5FtyQ+GDN5GOdX3fFd0/D7dHRI9gB2yxSD7d4iRpJyrwuF1xPgdI7Eo9I3wsytdKQ8vKpACVtDy3vcDiQDalDAmb5evhZ8Eqq0vmsYnLgSJVB9q+t8+UCAHF9YcAW5XMfAQJBAPZpalbBzWhTUTYNzxalah9q7HHFm+N+LXwzgVszXAmIF+X162bw3nnR5DaAR3adZOjMIGgAm4cFPeLKNeUt5SsCQQDTy1VHUdBgkXBNtFSiBAhiBS80ZjyViRVzKJ/Cv04uyo3kg5272cRw+VobE7iSS5Zo3YVRiKDe2VhLL4KETe9BAkARUWtV2Z0UHfbxM5tAXjPNLXicrmS8YlvUBNKslDl9ugDj+pqmy3r3WNcPNYjQP1OeQOpuJMzJeobU751GFCmtAkBGbYoZIkKZqiNfI4U1LwQenDy7PvgMfb5NQggGOZllA+Q1RfMQwXSKYNCuylsJAuusSWWI3FcqY7nxnoXXOxdBAkEAotoWufwgoJZk8WDqKrYt/7iCAXzHLnWiWoIgzpq5x7DSj/hfTdG1jBhRCrDd/kzqxUKlqxKX+O+52nkubVpfFQ==";
        Key privateKey = RSAEncrypterUtil.getPrivateKey(PRIVATEKEY);
        String aa = "XnmOj9musKWNKTskki/nHMN6siYt6a3uwf+h6HcZqNVNqb0wpkl1MgJd2gac/LnftJJ9gBq2GTeo4XoXWKb/7E45wiwepGEFHEtWFQwSgX9Qwo/ykoRlQjk9Kt6D2KqVKvexoQK9+H2ZN5k3cGf7LE7qdaqoq0QbGPwsTmoYm1s=";
        byte[] bb = Base64.decodeBase64(aa);

        byte[] desKey = RSAEncrypterUtil.decryptByPrivateKey(bb, privateKey);

        System.out.println(new String(desKey));
    }

    @Test
    public void testRoundtrip() {
        String _privateKey = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAOtzh6UgLUNpOxaxocUZbfOxO68gq/E9v/7RlBAE1ZD7ngxEiFQVAhulwE6F3xHyptPHDRuPQx0en8RBulOnZwo7ogUe6NIMLQBl7WqSwh6S+5dGlh8iSx8WurlikfyNFfhWKzVP1l1UAo8rxckQKVepl/wZZdRuX6kIakL33bv1AgMBAAECgYEAnQnGPBVU+zQxi/ZX/jzyv+nC9BBnfhSAfPXNuk1vIC8if+4pu2Lzr/sCY50YZkEgkfmePXZ0/fQR/XUVUHxD1SC2xKp9HVAEQcGRFNfEWz6wck1K9xpa5jXn1hWehV6qP9GuQSx6kWv27epkp7agCtNkRPMA9ATKF8eC87W6JoECQQD9+f/ay/8oSmHy74XdO++TUm0F56/S9jLgn47YBjWC3mAq8nLFKDKDq/cWL7b8fugYRE1f1NqDNl0fI6xQ1b7hAkEA7VO/PJINOL0nwOXa9WnF3Z4vEv96D5IEFY35YLhaPuZjnhZ22JlFc92VrmOCcNuzBaOurqlr+PV8Oxy9zlkDlQJADVa9+03Pftw5PLsburzVHlWr6C187IWFsopuxCW/Vbo8LsVBBd3QmV3EwmOLJsO7iNq9fZT+TPnj9ii3eh9fwQJBAKGkgcMigujbROFIbQrsAP2CCPP6l8tVG30G1wx+Y9EloEIDcOzz7+8LxDAjzKtLqDI2GvsRn93Qbc3hNJH/5MUCQBIIEFPnVbONwxYZd5/sQ8a89k+v8rvn2pcEO3wMBwotOhGZL3kFZ342ikylCKSxMygXKOSUmi/ign6VpqPaSEA=";
        Key privateKey = RSAEncrypterUtil.getPrivateKey(_privateKey);

        String _publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDrc4elIC1DaTsWsaHFGW3zsTuv" +
                "IKvxPb/+0ZQQBNWQ+54MRIhUFQIbpcBOhd8R8qbTxw0bj0MdHp/EQbpTp2cKO6IF" +
                "HujSDC0AZe1qksIekvuXRpYfIksfFrq5YpH8jRX4Vis1T9ZdVAKPK8XJEClXqZf8" +
                "GWXUbl+pCGpC99279QIDAQAB";

        Key publicKey = RSAEncrypterUtil.getPublicKey(_publicKey);

        String text = "The quick brown fox jumps over the lazy dog";

        byte[] b1 = RSAEncrypterUtil.encryptByPublicKey(text.getBytes(), publicKey);
        byte[] b2 = RSAEncrypterUtil.decryptByPrivateKey(b1, privateKey);

        assertEquals(text, new String(b2));


        b1 = RSAEncrypterUtil.encryptByPrivateKey(text.getBytes(), privateKey);
        b2 = RSAEncrypterUtil.decryptByPublicKey(b1, publicKey);

        assertEquals(text, new String(b2));

    }

    @Test
    public void testRoundtrip2() {
        String _privateKey = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAOtzh6UgLUNpOxaxocUZbfOxO68gq/E9v/7RlBAE1ZD7ngxEiFQVAhulwE6F3xHyptPHDRuPQx0en8RBulOnZwo7ogUe6NIMLQBl7WqSwh6S+5dGlh8iSx8WurlikfyNFfhWKzVP1l1UAo8rxckQKVepl/wZZdRuX6kIakL33bv1AgMBAAECgYEAnQnGPBVU+zQxi/ZX/jzyv+nC9BBnfhSAfPXNuk1vIC8if+4pu2Lzr/sCY50YZkEgkfmePXZ0/fQR/XUVUHxD1SC2xKp9HVAEQcGRFNfEWz6wck1K9xpa5jXn1hWehV6qP9GuQSx6kWv27epkp7agCtNkRPMA9ATKF8eC87W6JoECQQD9+f/ay/8oSmHy74XdO++TUm0F56/S9jLgn47YBjWC3mAq8nLFKDKDq/cWL7b8fugYRE1f1NqDNl0fI6xQ1b7hAkEA7VO/PJINOL0nwOXa9WnF3Z4vEv96D5IEFY35YLhaPuZjnhZ22JlFc92VrmOCcNuzBaOurqlr+PV8Oxy9zlkDlQJADVa9+03Pftw5PLsburzVHlWr6C187IWFsopuxCW/Vbo8LsVBBd3QmV3EwmOLJsO7iNq9fZT+TPnj9ii3eh9fwQJBAKGkgcMigujbROFIbQrsAP2CCPP6l8tVG30G1wx+Y9EloEIDcOzz7+8LxDAjzKtLqDI2GvsRn93Qbc3hNJH/5MUCQBIIEFPnVbONwxYZd5/sQ8a89k+v8rvn2pcEO3wMBwotOhGZL3kFZ342ikylCKSxMygXKOSUmi/ign6VpqPaSEA=";
        Key privateKey = RSAEncrypterUtil.getPrivateKey(_privateKey);

        String _publicKey = "MIIDGTCCAoKgAwIBAgIJAKQp2GIWY/q7MA0GCSqGSIb3DQEBBQUAMGcxCzAJBgNVBAYTAkNOMRAwDgYDVQQIEwdCZWlqaW5nMREwDwYDVQQHEwhCZWlqaW5nIDEVMBMGA1UEChMMUmVuUmVuIEdhbWVzMQ0wCwYDVQQLEwRBZGVyMQ0wCwYDVQQDEwRhZGVyMB4XDTEzMDUyMTA4MjExNFoXDTEzMDYyMDA4MjExNFowZzELMAkGA1UEBhMCQ04xEDAOBgNVBAgTB0JlaWppbmcxETAPBgNVBAcTCEJlaWppbmcgMRUwEwYDVQQKEwxSZW5SZW4gR2FtZXMxDTALBgNVBAsTBEFkZXIxDTALBgNVBAMTBGFkZXIwgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBAOtzh6UgLUNpOxaxocUZbfOxO68gq/E9v/7RlBAE1ZD7ngxEiFQVAhulwE6F3xHyptPHDRuPQx0en8RBulOnZwo7ogUe6NIMLQBl7WqSwh6S+5dGlh8iSx8WurlikfyNFfhWKzVP1l1UAo8rxckQKVepl/wZZdRuX6kIakL33bv1AgMBAAGjgcwwgckwHQYDVR0OBBYEFJVWdYTPmBGcuy1+/cfTj1mKUlgNMIGZBgNVHSMEgZEwgY6AFJVWdYTPmBGcuy1+/cfTj1mKUlgNoWukaTBnMQswCQYDVQQGEwJDTjEQMA4GA1UECBMHQmVpamluZzERMA8GA1UEBxMIQmVpamluZyAxFTATBgNVBAoTDFJlblJlbiBHYW1lczENMAsGA1UECxMEQWRlcjENMAsGA1UEAxMEYWRlcoIJAKQp2GIWY/q7MAwGA1UdEwQFMAMBAf8wDQYJKoZIhvcNAQEFBQADgYEA5lguAldxtorlV00y/vTj7t5JIc44LX5F/j+levDmEYJpb6h09iVanHKMFtXGiOslqc47NuXEACN2+IRivjnRyWsZVoPPtEwFGxQjjEQjsPSslNSbxvwkPvZYrflfItXLjx/MBOuVgITdtbsbNNm02M9IWlSuiztqMAHPSuposh0=";

        Key publicKey = RSAEncrypterUtil.getDERPublicKey(_publicKey);

        String text = "The quick brown fox jumps over the lazy dog";

        byte[] b1 = RSAEncrypterUtil.encryptByPublicKey(text.getBytes(), publicKey);
        byte[] b2 = RSAEncrypterUtil.decryptByPrivateKey(b1, privateKey);

        assertEquals(text, new String(b2));


        b1 = RSAEncrypterUtil.encryptByPrivateKey(text.getBytes(), privateKey);
        b2 = RSAEncrypterUtil.decryptByPublicKey(b1, publicKey);

        assertEquals(text, new String(b2));

    }

    @Test
    public void testRoundtrip3() {
        String _privateKey = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBANHXgklh8dJVDslSenNLSs5zrxOjFIGeOuqdEShXt4cHV4pCmT1BtJE9hmWMhPuDBnGcaeXJBbdyIDVDgpzrQQbOzgov681SM/TKT4DWbyb7YjFvynGbDw5cac57WRGNvsiTaQsaz/aZoJrK0hTF0ODhTKnYCh+SMrp5irl51x47AgMBAAECgYBYYoGKDysgAJudyJOzBD+Z/bf8eXAluFPwhf+4ElpHEZyZq7uHxakck+GL3EgW9/U63TrtgmJaBkPyq2DUX3Kfe+zNnHQ2oRjVY6lMNJBLTyu9Qs99Nqqj7+lNlq0W9Re6xb9FIB5jya6YZvMi6mIk1GlZdtdlBJCfXRMTRn6BIQJBAPSAWGksYPhGppztCaqlCZXn7YPwqB2gOel9maVw6O2rtGAdWO+5IhQ4mfyrlfgjZ+4NyMVylLwwDkbwZy7FRdcCQQDbteIQVFXb4SbjUOIaDY6B3WtLLkEt03wGercw/W/K28UZDMEfqwejixOiwJM6sXL0Oh0OIel362oAph1hMxY9AkAaP90+DWGrxgoFNv2esHFDAs9hJuklpRoSk0V3mZOoUJLg7sWplvqtqRX/JnzHUyXJTPmNGSwWuCIQQ6cQQmWpAkBEL49ICBRZV+fMtkZlVX27me75dxJtWWvtStpdjtnJ+CiUHJw26spFHB1s7h0DIx0M5Jgt0aJ2QA42w1bs2oD1AkEA7AxtItO+iapBojC8BuWaBe2vCharMbl3nAR509rVfY8UfrGyS+qWZs8VcBd9Kj7FwN2LQT6VqySyKD+uoidp7w==";
        Key privateKey = RSAEncrypterUtil.getPrivateKey(_privateKey);

        String _publicKey = "MIIDGTCCAoKgAwIBAgIJAIyAsT8TSpVsMA0GCSqGSIb3DQEBBQUAMGcxCzAJBgNVBAYTAkNOMRAwDgYDVQQIEwdCZWlqaW5nMRAwDgYDVQQHEwdCZWlqaW5nMRUwEwYDVQQKEwxSZW5SZW4gR2FtZXMxDjAMBgNVBAsTBVRlY2g2MQ0wCwYDVQQDEwRBZGVyMB4XDTEzMDUyMzA2NDIwNloXDTEzMDYyMjA2NDIwNlowZzELMAkGA1UEBhMCQ04xEDAOBgNVBAgTB0JlaWppbmcxEDAOBgNVBAcTB0JlaWppbmcxFTATBgNVBAoTDFJlblJlbiBHYW1lczEOMAwGA1UECxMFVGVjaDYxDTALBgNVBAMTBEFkZXIwgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBANHXgklh8dJVDslSenNLSs5zrxOjFIGeOuqdEShXt4cHV4pCmT1BtJE9hmWMhPuDBnGcaeXJBbdyIDVDgpzrQQbOzgov681SM/TKT4DWbyb7YjFvynGbDw5cac57WRGNvsiTaQsaz/aZoJrK0hTF0ODhTKnYCh+SMrp5irl51x47AgMBAAGjgcwwgckwHQYDVR0OBBYEFC1UHcq9Xu6vT0k95elTDq6LW90cMIGZBgNVHSMEgZEwgY6AFC1UHcq9Xu6vT0k95elTDq6LW90coWukaTBnMQswCQYDVQQGEwJDTjEQMA4GA1UECBMHQmVpamluZzEQMA4GA1UEBxMHQmVpamluZzEVMBMGA1UEChMMUmVuUmVuIEdhbWVzMQ4wDAYDVQQLEwVUZWNoNjENMAsGA1UEAxMEQWRlcoIJAIyAsT8TSpVsMAwGA1UdEwQFMAMBAf8wDQYJKoZIhvcNAQEFBQADgYEAoojzhQFUUQFclCtG0JDD5fA99uY2ABaEDrN6hIDoy5kzBVN1oI9nBxY8opHQ8RU7yJrUJlsKTjCIXGKMVIhXCZZcnpBK1fJkzB8AlHFypKDWAGOieqI8ZA1zeimshNQstLzBiWzjY98F1woSq+pGm1uULXbJ3nfbtsYxTLiSONU=";
        Key publicKey = RSAEncrypterUtil.getDERPublicKey(_publicKey);

        String text = "The quick brown fox jumps over the lazy dog";

        byte[] b1 = RSAEncrypterUtil.encryptByPublicKey(text.getBytes(), publicKey);

        System.out.println(Base64.encodeBase64String(b1));

        b1 = Base64.decodeBase64("Nl9UBSr7WOO8HKGQzNI6f608423TkLImksriPXspPlH8IdPIp+8LNkAg5AwWJiRT2+gR4tt+O8yMueXoZXC6OXg8Zv+8L8qddB8j2vAVXbrWoc702K3u4CpjZ9BhhluY8RRIyCpFo9HkX5dZNoourN+sdW3ZbVSoIHWGeFPUmVk=");

        byte[] b2 = RSAEncrypterUtil.decryptByPrivateKey(b1, privateKey);

        String actual = new String(b2);

        System.out.println(actual);

        assertEquals(text, actual);


        b1 = RSAEncrypterUtil.encryptByPrivateKey(text.getBytes(), privateKey);
        b2 = RSAEncrypterUtil.decryptByPublicKey(b1, publicKey);

        assertEquals(text, new String(b2));

    }
}

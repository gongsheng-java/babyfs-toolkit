package com.babyfs.tk.commons.name.impl.zookeeper;

import com.babyfs.tk.commons.Constants;
import com.babyfs.tk.commons.zookeeper.ZkClient;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
import org.junit.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

/**
 */
public class ZkNameServiceRegisterTest {
    public static void main(String[] args) {
        ZkClient zkClient = new ZkClient("127.0.0.1:2181", "gsns", "gsns@zookeeper");
        ZkNameServiceRegister reg = new ZkNameServiceRegister(zkClient, "svr_" + args[0], "127.0.0.1", 9123, "/services",new ServerNodeJsonCodec());
        reg.addService("test_auto");
        reg.addService("test_auto_1");
        boolean register = reg.register();
        System.out.println("register:" + register);
        try {
            System.in.read();
            zkClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void test_acl() throws NoSuchAlgorithmException {
        try {
            String password = Base64.encodeBase64String(DigestUtils.sha("gsns@zookeeper".getBytes(Constants.DEFAULT_CHARSET)));
            System.out.println(password);
            password = DigestAuthenticationProvider.generateDigest("gsns" + ":" + "gsns@zookeeper");
            System.out.println(password);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }
}

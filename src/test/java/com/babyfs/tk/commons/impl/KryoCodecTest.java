package com.babyfs.tk.commons.impl;

import com.babyfs.tk.commons.codec.impl.HessianCodec;
import com.babyfs.tk.commons.codec.impl.KryoCodec;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.babyfs.tk.commons.base.Pair;
import com.babyfs.tk.commons.codec.ICodec;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class KryoCodecTest {

    public static final int THREADS = 10;

    @Test
    public void testEncodeAndDecode() throws Exception {
        KryoCodec kryoCodec = new KryoCodec();
        testCodec(kryoCodec, "Default kryo");
        List<Pair<? extends Class, Integer>> registeredClasses = Lists.newArrayList();
        {
            registeredClasses.add(Pair.of(User.class, 10));
            registeredClasses.add(Pair.of(User2.class, 11));
        }
        KryoCodec kryoCodecWithClasses = new KryoCodec(false, false, registeredClasses, 512);
        testCodec(kryoCodecWithClasses, "kryo with classes");
        HessianCodec hessianCodec = new HessianCodec();
        testCodec(hessianCodec, "Hessin");
    }

    private void testCodec(ICodec codec, String desc) {
        System.out.println(desc);
        Object o1 = "This is a test";
        test(o1, codec, "String Test");
        Map<String, User> o2 = Maps.newHashMap();
        for (int i = 0; i < 100; i++) {
            User user = new User();
            user.setName("name" + i);
            user.setId(i);
            o2.put(user.getName(), user);
        }
        test(o2, codec, "Map<String,User> Test");

        List<User> o2list = Lists.newArrayList();
        for (int i = 0; i < 100; i++) {
            User user = new User();
            user.setName("name" + i);
            user.setId(i);
            o2list.add(user);
        }
        test(o2list, codec, "List<User> Test");

        User o3 = new User2();
        o3.setId(10000);
        o3.setName("abcde");
        test(o3, codec, "User Test");

        test(1797998L, codec, "Long Test");

        System.out.println();

    }

    private void test(Object o, ICodec codec, String desc) {
        byte[] encode = codec.encode(o);
        System.out.println(desc + ":length:" + encode.length);
        Object o1R = codec.decode(encode);
        Assert.assertEquals(o, o1R);
    }

    public static class User implements Serializable {
        String name;
        int id;
        int f1;
        int f2;
        int f3;
        int f4;
        int f5;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            User user = (User) o;

            if (id != user.id) return false;
            if (name != null ? !name.equals(user.name) : user.name != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + id;
            return result;
        }
    }

    public static class User2 extends User {
        private String user2;
    }

    @Test
    @Ignore
    public void test_compatibility() throws IOException {
        File hessianV1 = new File("hessian-v1.data");
        File kryoV1 = new File("kryo-v1.data");

        User user = new User();
        user.setName("myname");
        user.setId(1898);
        user.f4 = 1000;

        ICodec hessianCodec = new HessianCodec();
        ICodec kryoCodec = new KryoCodec(true, false, null, 8 * 1024); //兼容
        //ICodec kryoCodec = new KryoCodec(false, false, null, 512); //不兼容
        {
            byte[] encodeByHessian = hessianCodec.encode(user);
            byte[] encodeByKryo = kryoCodec.encode(user);
            System.out.println("encodeByHessian:" + encodeByHessian.length + ",encodeByKryo=" + encodeByKryo.length);
            Files.write(encodeByHessian, hessianV1);
            Files.write(encodeByKryo, kryoV1);
        }

        byte[] hessianV1Data = Files.asByteSource(hessianV1).read();
        byte[] kryoV1Data = Files.asByteSource(kryoV1).read();

        Object hessianUser = hessianCodec.decode(hessianV1Data);
        Object kryoUser = kryoCodec.decode(kryoV1Data);
    }

    @Test
    @Ignore
    public void test_performance() throws IOException {
        User user = new User();
        user.setName("myname");
        user.setId(1898);
        user.f4 = 1000;

        ICodec hessianCodec = new HessianCodec();
        ICodec kryoCodec = new KryoCodec(true, false, null, 8 * 1024); //兼容
        //ICodec kryoCodec = new KryoCodec(false, false, null, 512); //不兼容

        codec_performance(hessianCodec, user, false);
        codec_performance(kryoCodec, user, false);

        System.gc();
        System.gc();
        System.out.println("hessian performnace:");
        codec_performance(hessianCodec, user, true);
        System.out.println();

        System.gc();
        System.gc();
        System.out.println("kryo performnace:");
        codec_performance(kryoCodec, user, true);
        System.out.println();
    }

    @Test
    public void test_in_multithread() throws IOException {
        final User user = new User();
        user.setName("myname");
        user.setId(1898);
        user.f4 = 1000;

        final ICodec hessianCodec = new HessianCodec();
        final ICodec kryoCodec = new KryoCodec(true, false, null, 8 * 1024); //兼容

        final AtomicInteger num = new AtomicInteger();
        final AtomicInteger fail = new AtomicInteger();
        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        for (int i = 0; i < 10; i++) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < 100; i++) {
                        byte[] hbytes = hessianCodec.encode(user);
                        byte[] kbytes = kryoCodec.encode(user);
                        Object huser = hessianCodec.decode(hbytes);
                        Object kuser = kryoCodec.decode(kbytes);

                        if (!Objects.equals(huser, user)) {
                            fail.incrementAndGet();
                        }
                        if (!Objects.equals(kuser, user)) {
                            fail.incrementAndGet();
                        }
                        num.incrementAndGet();
                    }
                }
            });
        }
        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(num.get());
        Assert.assertEquals(THREADS * 100, num.get());
        Assert.assertEquals(0, fail.get());
    }

    private void codec_performance(ICodec codec, Object o, boolean print) {
        long st = System.nanoTime();
        int len = 0;
        for (int i = 0; i < 1000000; i++) {
            byte[] encode = codec.encode(o);
            len = encode.length;
        }
        long et = System.nanoTime();
        if (print) {
            System.out.println("codec:" + codec.getClass() + " encode time(ms):" + (et - st) / (1000 * 1000) + " len:" + len);
        }

        byte[] encode = codec.encode(o);
        st = System.nanoTime();
        for (int i = 0; i < 1000000; i++) {
            codec.decode(encode);
        }
        et = System.nanoTime();
        if (print) {
            System.out.println("codec:" + codec.getClass() + " decode time(ms):" + (et - st) / (1000 * 1000) + " len:" + len);
        }
    }
}

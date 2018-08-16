package com.babyfs.tk.service.biz.serialnum;

import com.babyfs.tk.service.biz.serialnum.enums.SerialNumType;
import com.google.inject.Inject;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

@Ignore
public class SerialNumSeriviceTest extends BaseTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(SerialNumSeriviceTest.class);

    @Inject
    private ISerialNumService service;

    @Test
    public void createNumber() {
        System.out.println(service.getSerialNum(SerialNumType.FISSION_TRANSACTION));
    }

    @Test
    public void createNumber2() {
        System.out.println(service.getSFSerialNum(SerialNumType.FISSION_TRANSACTION));
    }

    @Test
    public void asnyCreateNum() {
        int pool = 500;
        ExecutorService executor = Executors.newFixedThreadPool(pool);
        int count = 0;
        for (;;) {
            List<CompletableFuture<Void>> list = new ArrayList<>();
            for (int i = 0; i < pool; i++) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> System.out.println(service.getSerialNum(SerialNumType.FISSION_TRANSACTION)), executor);
                list.add(future);
            }
            CompletableFuture[] newList = list.toArray(new CompletableFuture[0]);
            CompletableFuture<Void> all = CompletableFuture.allOf(newList);
            all.join();
            count++;
            if (count > 20) break;
        }
    }


    @Test
    public void asnyCreateNum2() {
        int pool = 500;
        ExecutorService executor = Executors.newFixedThreadPool(pool);
        int count = 0;
        for (;;) {
            List<CompletableFuture<Void>> list = new ArrayList<>();
            for (int i = 0; i < pool; i++) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> System.out.println(service.getSFSerialNum(SerialNumType.FISSION_TRANSACTION)), executor);
                list.add(future);
            }
            CompletableFuture[] newList = list.toArray(new CompletableFuture[0]);
            CompletableFuture<Void> all = CompletableFuture.allOf(newList);
            all.join();
            count++;
            if (count > 20) break;
        }
    }

}

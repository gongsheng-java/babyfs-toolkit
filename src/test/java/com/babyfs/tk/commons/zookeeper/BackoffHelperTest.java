package com.babyfs.tk.commons.zookeeper;

import com.google.common.base.Function;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class BackoffHelperTest {

    @Test
    public void doRepeatTask() throws InterruptedException {
        BackoffHelper helper = new BackoffHelper();

        final int maxValue = 20;
        CountDownLatch countDownLatch = new CountDownLatch(maxValue);
        AtomicInteger counter = new AtomicInteger();

        helper.doRepeatTask(new Function<Void, Boolean>() {
            @Nullable
            @Override
            public Boolean apply(@Nullable Void input) {
                int value = counter.incrementAndGet();
                countDownLatch.countDown();
                if (value >= maxValue) {
                    return false;
                }
                return true;
            }
        }, 10, 2);

        countDownLatch.await();
        System.out.println(counter.get());
        Assert.assertEquals(maxValue, counter.get());
    }
}
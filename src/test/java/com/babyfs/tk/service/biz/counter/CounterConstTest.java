package com.babyfs.tk.service.biz.counter;

import com.babyfs.tk.service.basic.redis.LuaScript;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 *
 */
public class CounterConstTest {
    @Test
    public void loadScript() throws Exception {
        String loadScript = LuaScript.loadScript("lua/counter_update.lua");
        System.out.println(loadScript);
    }

    @Test
    public void testLocalTime() {
        LocalDateTime now = LocalDateTime.now();
        System.out.println(now);
        ZoneId zoneId = ZoneOffset.systemDefault();
        now.atZone(zoneId);
    }

    @Test
    public void testReal() {
        long l = CounterConst.realCounterEpochSecond(238299726);
        System.out.println(l);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(l), ZoneId.systemDefault());
        System.out.println(localDateTime);
    }
}
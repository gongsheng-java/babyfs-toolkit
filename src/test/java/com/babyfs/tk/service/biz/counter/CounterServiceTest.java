package com.babyfs.tk.service.biz.counter;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.babyfs.tk.service.biz.Modules;
import com.babyfs.tk.service.biz.counter.guice.IDSequenceServiceModule;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

/**
 * Created by IntelliJ IDEA.
 * <p/>
 */
public class CounterServiceTest {

    private static final String BIZ_KEY = "bizkey";

    @Test
    @Ignore
    public void testProcess() throws Exception {
        Injector injector = Guice.createInjector(
                Modules.BASIC_MODULE_REDIS_CONF,
                Modules.BASIC_MODULE_REDIS_SERVICE,
                new IDSequenceServiceModule()
        );
        IDSequenceService service = injector.getInstance(IDSequenceService.class);
        Date date = new Date();

        Long value = service.getDailyNext(BIZ_KEY, date);
        assertNotNull(value);
        assertSame(1L, value);
        System.out.println("value is: " + value);

        TimeUnit.SECONDS.sleep(6);  // 由 CounterConst.CACHE_TIME_DAILY_COUNTER  决定
        // 过期清零
        value = service.getDailyNext(BIZ_KEY, date);
        assertNotNull(value);
        assertSame(1L, value);
        System.out.println("value is: " + value);

    }

}

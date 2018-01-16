package com.babyfs.tk.http.client;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.babyfs.tk.http.guice.AsyncHttpClientModule;
import com.babyfs.tk.http.guice.HttpClientModule;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Response;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;

/**
 */
@Ignore
public class AsyncHttpClientServiceTest {
    @Inject
    private AsyncHttpClientService asyncHttpClientService;
    @Inject
    private HttpClientService httpClientService;

    Map<String, String> params = new HashMap<String, String>();

    private AtomicInteger s = new AtomicInteger(0);

    private AtomicInteger e = new AtomicInteger(0);

    private AtomicLongArray times = new AtomicLongArray(900);

    private AtomicLong totalTime = new AtomicLong(0);

    private long useTimes = 0;

    private String url = "http://mobads.baidu.com/cpro/ui/mads.php";


    @Before
    public void setUp() throws Exception {
        Module asyncHttpClientModule = new AsyncHttpClientModule();
        Module httpClientModule = new HttpClientModule();
        ArrayList<Module> modules = Lists.newArrayList(asyncHttpClientModule, httpClientModule);
        Injector injector = Guice.createInjector(modules);
        injector.injectMembers(this);
    }

    @Test
    public void clientTest() throws Exception {
        ListenableFuture<Response> future = asyncHttpClientService.sendGet("http://www.baidu.com", null, null, null);
        String s = asyncHttpClientService.getResponse(future);
        System.out.println(s);
    }


    @Test
    @Ignore
    public void httpTest() throws InterruptedException {
        final int thread = 100;
        final int batchsize = 50;
        final long starTime = System.currentTimeMillis();
        System.out.println("asyncHttpTest start");
        params.put("u", "default");
        params.put("ie", "1");
        params.put("n", "5");
        params.put("tm", "512");
        params.put("cm", "512");
        params.put("md", "1");
        params.put("at", "3");
        params.put("v", "partner");
        params.put("q", "debug_cpr");
        params.put("appid", "debug");
        params.put("w", "48");
        params.put("h", "320");
        params.put("tp", "G7");
        params.put("brd", "HTC");
        params.put("bdr", "10");
        params.put("sw", "480");
        params.put("sh", "854");
        params.put("sn", "3527840405342301");
        params.put("nop", "46000");
        params.put("im", "460030912121001");
        params.put("os", "android");
        params.put("cid", "61474_6318_0|61475_6319_0");
        params.put("wi", "c417fe076485_45|17fe0c448765_65");
        params.put("g", "1330582230666_127.12345_31.12345");
        params.put("ip", "123.123.123.123");
        params.put("swi", "1");
        params.put("tab", "0");
        params.put("act", "DL,LP,MAI");
        final CountDownLatch countDownLatch = new CountDownLatch(thread * batchsize);
        ExecutorService executorService = Executors.newFixedThreadPool(thread);
        Runnable run = new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < batchsize; i++) {
                    String response = null;
                    try {
                        long start = System.currentTimeMillis();
                        response = httpClientService.sendGet(url, params, null);
                        if (!Strings.isNullOrEmpty(response)) {
                            s.addAndGet(1);
                        } else {
                            e.addAndGet(1);
                        }
                        long times = (System.currentTimeMillis() - start);
                        System.out.println("times:" + times);
                        totalTime.addAndGet(times);
                    } catch (Exception ex) {
                        ex.printStackTrace();
//                        aaa.add(ex)
                        e.addAndGet(1);
                        continue;
                    } finally {
                        if ((s.get() + e.get()) == (thread * batchsize)) {
                            useTimes = System.currentTimeMillis() - starTime;
                        }
                        countDownLatch.countDown();
                    }
                }
            }
        };

        for (int i = 0; i < thread; i++) {
            executorService.submit(run);
        }
        boolean b = countDownLatch.await(160, TimeUnit.SECONDS);
        System.out.println("s:[" + s + "]");
        System.out.println("e:[" + e + "]");
        System.out.println("useTime:[" + useTimes + "]");
        System.out.println("totalTime:[" + totalTime + "]");
    }
}

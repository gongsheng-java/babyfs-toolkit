package com.babyfs.tk.service.biz.service.schedule;

import com.babyfs.tk.service.biz.service.schedule.internal.ScheduleServiceImpl;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 调度服务测试
 * <p/>
 */
public class ScheduleServiceTest {

    private static AtomicInteger count = new AtomicInteger(0);

    @Test
    @Ignore
    public void test() {

        IScheduleService scheduleService = new ScheduleServiceImpl(3);

        // 一次任务
        scheduleService.addOnceTimeTask(new Runnable() {
            @Override
            public void run() {
                System.out.println("This is once time sched task. count = " + count.getAndIncrement());
            }
        }, 0);


        // 定时任务，执行3次后取消
        scheduleService.addFixedDelayTaskWithCancel(new Runnable() {
            @Override
            public void run() {
                System.out.println("This is fixed time sched task with cancel. count = " + count.getAndIncrement());
            }
        }, 0, 1000, 2800);

        // 定时任务，能执行4次
        scheduleService.addFixedDelayTask(new Runnable() {
            @Override
            public void run() {
                System.out.println("This is fixed time sched task. count = " + count.getAndIncrement());
            }
        }, 1000, 1000);

        try {
            System.out.println("Start sleeping for 4.8 sec.");
            Thread.sleep(4800);
            scheduleService.shutDown(true);
            System.out.println("End sleeping.");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Assert.assertEquals(8, count.get());

    }

}

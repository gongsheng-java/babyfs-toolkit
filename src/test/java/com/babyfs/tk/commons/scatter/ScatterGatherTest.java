package com.babyfs.tk.commons.scatter;

import com.google.common.collect.ImmutableList;
import com.babyfs.tk.commons.concurrent.CallbackUtils;
import com.babyfs.tk.commons.concurrent.ICallback;
import com.babyfs.tk.commons.concurrent.scatter.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 */
public class ScatterGatherTest {
    private static class SumFunction implements IScatter<Integer> {
        private final int start;
        private final int end;

        private SumFunction(int start, int end) {
            this.start = start;
            this.end = end;
        }


        @Override
        public Integer call() {
            int sum = 0;
            for (int i = start; i <= end; i++) {
                sum += i;
            }
            return sum;
        }
    }

    private static class IntException implements IScatter<Integer> {
        @Override
        public Integer call() {
            throw new RuntimeException("IntExceptionTest");
        }
    }

    private static class WillTimeoutException implements IScatter<Integer> {
        private final long sleepTime;

        private WillTimeoutException(long sleepTime) {
            this.sleepTime = sleepTime;
        }

        @Override
        public Integer call() {
            try {
                Thread.sleep(this.sleepTime);
            } catch (InterruptedException e) {
            }
            return 1;
        }
    }


    private static class SumGather implements IGather<Integer, Integer> {
        private int sum = 0;

        @Override
        public synchronized void append(Integer result) {
            if (result == null) {
                return;
            }
            sum = sum + result.intValue();
        }

        @Override
        public synchronized Integer get() {
            return sum;
        }
    }

    @Test
    public void compute_test() {
        DefaultScatterTask<Integer> scatterTask = buileSummScatterTask();
        int result = ((Integer) scatterTask.getId()).intValue();
        SumGather gather = new SumGather();
        ScatterGather scatterGather = new ScatterGather();
        Integer computeResult = scatterGather.compute(scatterTask, gather);
        Assert.assertTrue(scatterTask.isDone());
        Assert.assertTrue(scatterTask.isSuccess());
        Assert.assertNotNull(computeResult);
        Assert.assertEquals(result, computeResult.intValue());
    }

    @Test
    public void comput_callback_test() throws InterruptedException {
        DefaultScatterTask<Integer> scatterTask = buileSummScatterTask();
        int result = ((Integer) scatterTask.getId()).intValue();
        SumGather gather = new SumGather();
        ScatterGather scatterGather = new ScatterGather();
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        scatterGather.computeWithCallback(scatterTask, gather, new IGatherCallback<Integer, Integer>() {
            @Override
            public void onFinish(IGather<Integer, Integer> integerIntegerIGather, Void out) {
                System.out.println("Finish");
                countDownLatch.countDown();
            }

            @Override
            public void onException(IGather<Integer, Integer> integerIntegerIGather, Exception e) {
                e.printStackTrace();
                countDownLatch.countDown();
            }
        }, 10L);
        countDownLatch.await(10, TimeUnit.MILLISECONDS);
        Assert.assertTrue(scatterTask.isDone());
        Assert.assertTrue(scatterTask.isSuccess());
        Assert.assertNotNull(gather.get());
        Assert.assertEquals(result, gather.get().intValue());
    }

    @Test
    public void comput_callback_exception() throws InterruptedException {
        ImmutableList.Builder<IScatter<Integer>> builder = ImmutableList.builder();
        builder.add(new IntException());
        builder.add(new IntException());
        DefaultScatterTask<Integer> scatterTask = new DefaultScatterTask<Integer>("exception_test", builder.build());
        SumGather gather = new SumGather();
        ScatterGather scatterGather = new ScatterGather();
        try {
            scatterGather.compute(scatterTask, gather);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        Assert.assertTrue(scatterTask.isDone());
        Assert.assertFalse(scatterTask.isSuccess());
    }

    @Test
    public void comput_with_sleep() throws InterruptedException {
        ImmutableList.Builder<IScatter<Integer>> builder = ImmutableList.builder();
        builder.add(new WillTimeoutException(10));
        builder.add(new WillTimeoutException(10));
        DefaultScatterTask<Integer> scatterTask = new DefaultScatterTask<Integer>("exception_test", builder.build());
        SumGather gather = new SumGather();
        ScatterGather scatterGather = new ScatterGather();
        Integer result = scatterGather.compute(scatterTask, gather);
        Assert.assertTrue(scatterTask.isDone());
        Assert.assertTrue(scatterTask.isSuccess());
        Assert.assertEquals(2, result.intValue());
    }

    private static class AsyncCompute implements IScatter<Integer> {
        private static final ExecutorService executor = Executors.newFixedThreadPool(2);

        @Override
        public Integer call() {
            final ICallback callback = CallbackUtils.getUpCallbackOnCurThread();
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    callback.onFinish(this, new Integer(2));
                }
            });
            return 0;
        }
    }

    @Test
    public void comput_async() throws InterruptedException {
        ImmutableList.Builder<IScatter<Integer>> builder = ImmutableList.builder();
        builder.add(new AsyncCompute());
        builder.add(new AsyncCompute());
        DefaultScatterTask<Integer> scatterTask = new DefaultScatterTask<Integer>("exception_test", builder.build());
        SumGather gather = new SumGather();
        ScatterGather scatterGather = new ScatterGather();
        Integer result = scatterGather.computeForAsyncScatter(scatterTask, gather);
        Assert.assertTrue(scatterTask.isDone());
        Assert.assertTrue(scatterTask.isSuccess());
        Assert.assertEquals(4, result.intValue());
    }

    private static class AsyncComputeFail implements IScatter<Integer> {
        private static final ExecutorService executor = Executors.newFixedThreadPool(2);

        @Override
        public Integer call() {
            final ICallback callback = CallbackUtils.getUpCallbackOnCurThread();
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    callback.onException(this, new Exception("2"));
                }
            });
            return 0;
        }
    }

    @Test
    public void comput_async_fail() throws InterruptedException {
        ImmutableList.Builder<IScatter<Integer>> builder = ImmutableList.builder();
        builder.add(new AsyncComputeFail());
        builder.add(new AsyncComputeFail());
        DefaultScatterTask<Integer> scatterTask = new DefaultScatterTask<Integer>("exception_test", builder.build());
        SumGather gather = new SumGather();
        ScatterGather scatterGather = new ScatterGather();
        try {
            scatterGather.computeForAsyncScatter(scatterTask, gather);
            Assert.assertTrue(false);
        } catch (Exception e) {

        }
        Assert.assertTrue(scatterTask.isDone());
        Assert.assertFalse(scatterTask.isSuccess());
    }

    @Test
    public void comput_with_timeout() throws InterruptedException {
        ImmutableList.Builder<IScatter<Integer>> builder = ImmutableList.builder();
        builder.add(new WillTimeoutException(100));
        builder.add(new WillTimeoutException(100));
        DefaultScatterTask<Integer> scatterTask = new DefaultScatterTask<Integer>("exception_test", builder.build());
        SumGather gather = new SumGather();
        ScatterGather scatterGather = new ScatterGather();
        final AtomicBoolean finish = new AtomicBoolean(false);
        final AtomicBoolean success = new AtomicBoolean(false);
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        scatterGather.computeWithCallback(scatterTask, gather, new IGatherCallback<Integer, Integer>() {
            @Override
            public void onFinish(IGather<Integer, Integer> integerIntegerIGather, Void out) {
                finish.set(true);
                success.set(true);
                countDownLatch.countDown();
            }

            @Override
            public void onException(IGather<Integer, Integer> integerIntegerIGather, Exception e) {
                finish.set(true);
                success.set(false);
                Assert.assertTrue(e instanceof TimeoutException);
                countDownLatch.countDown();
            }
        }, 10L);
        countDownLatch.await(20L, TimeUnit.MILLISECONDS);
        Assert.assertTrue(scatterTask.isDone());
        Assert.assertFalse(scatterTask.isSuccess());
        Assert.assertTrue(finish.get());
        Assert.assertFalse(success.get());
    }

    private DefaultScatterTask<Integer> buileSummScatterTask() {
        final int batch = 5;
        final int step = 10;
        ImmutableList.Builder<IScatter<Integer>> builder = ImmutableList.builder();
        for (int i = 0; i < batch; i++) {
            int start = (i * step) + 1;
            int end = (i + 1) * step;
            builder.add(new SumFunction(start, end));
        }
        int result = 0;
        for (int i = 1; i <= (step * batch); i++) {
            result = result + i;
        }
        return new DefaultScatterTask<Integer>(result, builder.build());
    }
}

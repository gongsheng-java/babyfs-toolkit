package com.babyfs.tk.probe.metrics;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import com.codahale.metrics.jvm.CachedThreadStatesGaugeSet;
import com.codahale.metrics.jvm.FileDescriptorRatioGauge;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

/**
 * Metric探针
 */
public class MetricsProbe {
    private final static Logger LOGGER = LoggerFactory.getLogger(MetricsProbe.class);
    private final static MetricRegistry registry = new MetricRegistry();
    private static final long ONE_MILLIS_NANOS = TimeUnit.MILLISECONDS.toNanos(1);

    // 构建MetricProcbe,并注册JVM的相关的Metrics
    static {
        registry.register(Const.JVM_MEMORY, new MemoryUsageGaugeSet());
        registry.register(Const.JVM_GC, new GarbageCollectorMetricSet());
        registry.register(Const.JVM_THREAD_STATES, new CachedThreadStatesGaugeSet(5, TimeUnit.MINUTES));
        registry.register(Const.JVM_FD_USAGE, new FileDescriptorRatioGauge());
        registry.register(Const.JVM_PROCESS, new JVMProcessUsageGaugeSet());
    }

    /**
     * 更新由name指定的Timer
     *
     * @param name    Timer的名称
     * @param startNs 由{@link System#nanoTime()}获得开始时间
     */
    public static void timerUpdateNsFromStart(String name, long startNs) {
        long duration = System.nanoTime() - startNs;
        timerUpdateNs(name, duration);
    }

    /**
     * 更新由name指定的Timer
     *
     * @param name       Timer的名称
     * @param durationNs 经历的纳秒
     */
    public static void timerUpdateNs(String name, long durationNs) {
        if (Strings.isNullOrEmpty(name)) {
            return;
        }
        if (durationNs < 0) {
            return;
        }

        try {
            Timer timer = registry.timer(name);
            if (timer == null) {
                return;
            }
            timer.update(durationNs, TimeUnit.NANOSECONDS);
        } catch (Throwable e) {
            //此处捕获所有的异常,避免影响业务逻辑
            LOGGER.error("update timer " + name + " fail.", e);
        }
    }

    /**
     * 更新由moduleName和itemName决定的timer.
     * <p>
     * metrics的名称格式为moduleName-itemName-result,当sucess为true,result=ok;否则result=fail
     *
     * @param moduleName 模块的名称
     * @param itemName   指标项的名称
     * @param startNs    开始的时间,由{@link System#nanoTime()}获得
     * @param success    是否成功,true:成功;false:失败
     */
    public static void timerUpdateNSFromStart(String moduleName, String itemName, long startNs, boolean success) {
        long duration = System.nanoTime() - startNs;
        String tags = Joiner.on(":").join("timer", moduleName, success ? "succ" : "fail");
        String metricsName = itemName + "$" + tags + "$";
        timerUpdateNs(metricsName, duration);
    }

    /**
     * 收集所有的Timer数据,并格式化为k=v
     *
     * @return
     */
    public static List<String> collectAllTimer() {
        List<String> result = Lists.newLinkedList();
        collectAllTimer(result);
        return result;
    }

    /**
     * 收集所有的Timer数据,格式为k=v,将结果保存到result中
     *
     * @param result
     */
    public static void collectAllTimer(List<String> result) {
        SortedMap<String, Timer> timers = registry.getTimers();
        if (timers == null || timers.isEmpty()) {
            return;
        }
        if (result == null) {
            return;
        }
        for (Map.Entry<String, Timer> kv : timers.entrySet()) {
            formatTimer(kv.getKey(), kv.getValue(), result);
        }
    }

    /**
     * 收集所有的Gauge数据,并格式化为k=v
     *
     * @return
     */
    public static List<String> collectAllGauage() {
        List<String> result = Lists.newLinkedList();
        collectAllGauage(result);
        return result;
    }

    /**
     * 收集所有的Gauge数据,并格式化为k=v,将结果保存到result中
     *
     * @param result
     */
    public static void collectAllGauage(List<String> result) {
        SortedMap<String, Gauge> gauges = registry.getGauges();
        if (gauges == null || gauges.isEmpty()) {
            return;
        }
        if (result == null) {
            return;
        }
        for (Map.Entry<String, Gauge> kv : gauges.entrySet()) {
            formatGuage(kv.getKey(), kv.getValue(), result);
        }
    }

    /**
     * 取得MericsRegistry实例
     *
     * @return
     */
    public static MetricRegistry getRegistry() {
        return registry;
    }

    /**
     * 格式化输出Timer的数据到result中,格式为[timername].[itemname]=value
     *
     * @param name   timer的名称
     * @param timer  timer
     * @param result 结果列表
     */
    private static void formatTimer(String name, Timer timer, List<String> result) {
        if (Strings.isNullOrEmpty(name) || timer == null || result == null) {
            return;
        }
        Snapshot snapshot = timer.getSnapshot();
        result.add(formatInteger(name, "count", timer.getCount()));
        result.add(formatFloat(name, "rate1", timer.getOneMinuteRate()));
        result.add(formatFloat(name, "rate5", timer.getFiveMinuteRate()));
        result.add(formatFloat(name, "ratemeanr", timer.getMeanRate()));
        result.add(formatInteger(name, "min", snapshot.getMin() / ONE_MILLIS_NANOS));
        result.add(formatInteger(name, "max", snapshot.getMax() / ONE_MILLIS_NANOS));
        result.add(formatFloat(name, "mean", snapshot.getMean() / ONE_MILLIS_NANOS));
        result.add(formatFloat(name, "75th", snapshot.get75thPercentile() / ONE_MILLIS_NANOS));
        result.add(formatFloat(name, "90th", snapshot.getValue(0.9) / ONE_MILLIS_NANOS));
        result.add(formatFloat(name, "99th", snapshot.get99thPercentile() / ONE_MILLIS_NANOS));
        result.add(formatFloat(name, "99.9th", snapshot.get999thPercentile() / ONE_MILLIS_NANOS));
    }

    /**
     * 格式化输出Gauge的数据到result中,格式为[name].val=value
     *
     * @param name   gauge的名称
     * @param gauge  gauage
     * @param result 结果列表
     */
    private static void formatGuage(String name, Gauge gauge, List<String> result) {
        if (Strings.isNullOrEmpty(name) || gauge == null || result == null) {
            return;
        }
        Object value = gauge.getValue();
        if (!(value instanceof Number)) {
            return;
        }
        result.add(formatObject(name, "", value));
    }

    private static String formatInteger(String name, String itemName, long value) {
        return String.format("%s=%d", getName(name, itemName), value);
    }


    private static String formatFloat(String name, String itemName, double value) {
        return String.format("%s=%f", getName(name, itemName), value);
    }

    private static String formatObject(String name, String itemName, Object value) {
        return String.format("%s=%s", getName(name, itemName), value.toString());
    }

    private static String getName(String name, String itemName) {
        if (Strings.isNullOrEmpty(itemName)) {
            return name;
        }
        return name + "." + itemName;
    }
}

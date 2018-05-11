package com.babyfs.tk.probe.metrics;

import com.babyfs.tk.commons.base.Pair;
import com.babyfs.tk.commons.utils.ListUtil;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import com.codahale.metrics.jvm.CachedThreadStatesGaugeSet;
import com.codahale.metrics.jvm.FileDescriptorRatioGauge;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
     * 收集所有的Timer数据,格式为k=v,将结果保存到result中
     *
     * @param result
     */
    public static void collectAllTimer(List<String> result, MetricsFormat format) {
        SortedMap<String, Timer> timers = registry.getTimers();
        if (timers == null || timers.isEmpty()) {
            return;
        }
        if (result == null) {
            return;
        }
        for (Map.Entry<String, Timer> kv : timers.entrySet()) {
            formatTimer(kv.getKey(), kv.getValue(), result, format);
        }
    }


    /**
     * 收集所有的Gauge数据,按照format格式化,将结果保存到result中
     *
     * @param result
     */
    public static void collectAllGauage(List<String> result, MetricsFormat format) {
        SortedMap<String, Gauge> gauges = registry.getGauges();
        if (gauges == null || gauges.isEmpty()) {
            return;
        }
        if (result == null) {
            return;
        }
        for (Map.Entry<String, Gauge> kv : gauges.entrySet()) {
            formatGuage(kv.getKey(), kv.getValue(), result, format);
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
    private static void formatTimer(String name, Timer timer, List<String> result, MetricsFormat format) {
        if (Strings.isNullOrEmpty(name) || timer == null || result == null) {
            return;
        }


        Snapshot snapshot = timer.getSnapshot();

        List<Pair<String, String>> nameTagList = Lists.newArrayList();
        if (format == MetricsFormat.PROMETHEUS) {
            List<String> nameList = Splitter.on('$').splitToList(name);
            if (ListUtil.isEmpty(nameList) || nameList.size() < 2) {
                return;
            }
            name = nameList.get(0);

            String tags = nameList.get(1);
            List<String> tagList = Splitter.on(':').splitToList(tags);
            if (ListUtil.isEmpty(tagList) || tagList.size() < 3) {
                return;
            }
            String module = tagList.get(1);
            String success = tagList.get(2);
            nameTagList.add(Pair.of("module", module));
            nameTagList.add(Pair.of("result", success));
        }


        {
            String metricsName = "";
            String nameTags = "";
            if (format == MetricsFormat.PROMETHEUS) {
                metricsName =   format.formatName(name, "total");
                result.add(String.format("# TYPE %s counter", metricsName));
                nameTags = getTags(nameTagList);
            }else{
                metricsName =   format.formatName(name, "count");
            }
            result.add(format.formatIntValue(metricsName + nameTags, timer.getCount()));
        }

        List<Pair<String, Object>> rateItems = Lists.newArrayList(Pair.of("rate1", timer.getOneMinuteRate()),
                Pair.of("rate5", timer.getFiveMinuteRate()),
                Pair.of("ratemean", timer.getMeanRate()));

        formatTimerItem("rate", name, result, format, nameTagList, rateItems);

        List<Pair<String, Object>> responseItems = Lists.newArrayList(Pair.of("min", snapshot.getMin() / ONE_MILLIS_NANOS),
                Pair.of("max", snapshot.getMax() / ONE_MILLIS_NANOS),
                Pair.of("mean", snapshot.getMean() / ONE_MILLIS_NANOS),
                Pair.of("75th", snapshot.get75thPercentile() / ONE_MILLIS_NANOS),
                Pair.of("90th", snapshot.getValue(0.9) / ONE_MILLIS_NANOS),
                Pair.of("99th", snapshot.get99thPercentile() / ONE_MILLIS_NANOS),
                Pair.of("999th", snapshot.get999thPercentile() / ONE_MILLIS_NANOS));

        formatTimerItem("response", name, result, format, nameTagList, responseItems);
    }

    private static void formatTimerItem(String timerTypeName, String name, List<String> result, MetricsFormat format, List<Pair<String, String>> nameTagList, List<Pair<String, Object>> rateItems) {
        String proMetricsName = format.formatName(name, timerTypeName);
        if (format == MetricsFormat.PROMETHEUS) {
            result.add(String.format("# TYPE %s gauge", proMetricsName));
        }

        for (Pair<String, Object> item : rateItems) {
            final String metricsName;
            final String nameTags;
            if (format != MetricsFormat.PROMETHEUS) {
                metricsName = format.formatName(name, item.first);
                nameTags = "";
            } else {
                metricsName = proMetricsName;
                List<Pair<String, String>> allTags = Lists.newArrayList(nameTagList);
                allTags.add(Pair.of("timertype", item.first));
                nameTags = getTags(allTags);
            }
            result.add(format.formatObjectValue(metricsName + nameTags, item.second));
        }
    }


    /**
     * 格式化输出Gauge的数据到result中,格式为[name].val=value
     *
     * @param name   gauge的名称
     * @param gauge  gauage
     * @param result 结果列表
     */
    private static void formatGuage(String name, Gauge gauge, List<String> result, MetricsFormat format) {
        if (Strings.isNullOrEmpty(name) || gauge == null || result == null) {
            return;
        }
        Object value = gauge.getValue();
        if (!(value instanceof Number)) {
            return;
        }

        String metricsName = format.formatName(name, "");
        if (format == MetricsFormat.PROMETHEUS) {
            result.add(String.format("# TYPE %s gauge", metricsName));
        }

        result.add(format.formatObjectValue(metricsName, value));
    }

    private static String getTags(List<Pair<String, String>> nameTagList) {
        return "{" + Joiner.on(",").join(nameTagList.stream().map(t -> String.format("%s=\"%s\"", t.first, t.second)).collect(Collectors.toList())) + "}";
    }
}

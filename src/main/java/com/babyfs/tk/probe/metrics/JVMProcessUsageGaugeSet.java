package com.babyfs.tk.probe.metrics;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Java虚拟机 进程信息
 */
public class JVMProcessUsageGaugeSet implements MetricSet {
    private static final Logger LOGGER = LoggerFactory.getLogger(JVMProcessUsageGaugeSet.class);
    private static final String NAME_OPERATING_SYSTEM = "java.lang:type=OperatingSystem";
    private static final String NAME_PROCESS_CPU_LOAD = "ProcessCpuLoad";
    private static final String NAME_SYSTEM_CPU_LOAD = "SystemCpuLoad";

    private final MBeanServer platformMBeanServer;
    private final ObjectName name;
    private final LoadingCache<String, Double> jvmGauageCache;

    public JVMProcessUsageGaugeSet() {
        platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
        name = getOperatingSystemName();
        jvmGauageCache = CacheBuilder.newBuilder().expireAfterWrite(3, TimeUnit.SECONDS).build(new CacheLoader<String, Double>() {
            @Override
            public Double load(String key) {
                if (name == null) {
                    return -1D;
                }
                return getDoubleAttributeValue(name, key) * Runtime.getRuntime().availableProcessors();
            }
        });
    }


    @Override
    public Map<String, Metric> getMetrics() {
        final Map<String, Metric> gauges = new HashMap<String, Metric>();
        if (platformMBeanServer == null) {
            LOGGER.error("No platformMBeanServer");
            return gauges;
        }
        try {
            gauges.put("cpu_usage", new Gauge<Double>() {
                @Override
                public Double getValue() {
                    return getValueWithCache(NAME_PROCESS_CPU_LOAD);
                }
            });
            gauges.put("sys_cpu_usage", new Gauge<Double>() {
                @Override
                public Double getValue() {
                    return getValueWithCache(NAME_SYSTEM_CPU_LOAD);
                }
            });
        } catch (Exception e) {
            LOGGER.error("Setup " + NAME_OPERATING_SYSTEM + "fail", e);
            return gauges;
        }
        return gauges;
    }

    /**
     * @param name
     * @param attributeName
     * @return
     */
    private double getDoubleAttributeValue(ObjectName name, String attributeName) {
        Object processCpuLoad = null;
        try {
            processCpuLoad = platformMBeanServer.getAttribute(name, attributeName);
        } catch (Exception e) {
            LOGGER.error("Get " + name + "." + attributeName + " fail", e);
        }
        if (processCpuLoad != null && processCpuLoad instanceof Number) {
            return ((Number) processCpuLoad).doubleValue();
        }
        return Double.NaN;

    }

    private ObjectName getOperatingSystemName() {
        try {
            return ObjectName.getInstance(NAME_OPERATING_SYSTEM);
        } catch (MalformedObjectNameException e) {
            LOGGER.error("get name for {} fail", NAME_OPERATING_SYSTEM, e);
        }
        return null;
    }

    private Double getValueWithCache(String key) {
        try {
            return this.jvmGauageCache.get(key);
        } catch (ExecutionException e) {
            LOGGER.error("get key {} from cache fail", e);
            return Double.NaN;
        }

    }
}


package com.babyfs.tk.probe.metrics;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * Java虚拟机 进程信息
 */
public class JVMProcessUsageGaugeSet implements MetricSet {
    private static final Logger LOGGER = LoggerFactory.getLogger(JVMProcessUsageGaugeSet.class);
    private static final String NAME_OPERATING_SYSTEM = "java.lang:type=OperatingSystem";
    private static final String NAME_PROCESS_CPU_LOAD = "ProcessCpuLoad";
    private static final String NAME_SYSTEM_CPU_LOAD = "SystemCpuLoad";

    private final MBeanServer platformMBeanServer;

    public JVMProcessUsageGaugeSet() {
        platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
    }

    @Override
    public Map<String, Metric> getMetrics() {
        final Map<String, Metric> gauges = new HashMap<String, Metric>();
        if (platformMBeanServer == null) {
            LOGGER.error("No platformMBeanServer");
            return gauges;
        }
        try {
            final ObjectName name = ObjectName.getInstance(NAME_OPERATING_SYSTEM);
            gauges.put("cpu_usage", new Gauge<Double>() {
                @Override
                public Double getValue() {
                    return getDoubleAttributeValue(name, NAME_PROCESS_CPU_LOAD) * Runtime.getRuntime().availableProcessors();
                }
            });
            gauges.put("sys_cpu_usage", new Gauge<Double>() {
                @Override
                public Double getValue() {
                    return getDoubleAttributeValue(name, NAME_SYSTEM_CPU_LOAD) * Runtime.getRuntime().availableProcessors();
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
}

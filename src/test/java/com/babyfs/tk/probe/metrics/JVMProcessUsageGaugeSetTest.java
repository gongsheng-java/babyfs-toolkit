package com.babyfs.tk.probe.metrics;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class JVMProcessUsageGaugeSetTest {

    @Test
    @Ignore
    public void getMetrics() throws InterruptedException {
        JVMProcessUsageGaugeSet set = new JVMProcessUsageGaugeSet();
        for (int i = 0; i < 100; i++) {
            Map<String, Metric> metrics = set.getMetrics();
            for (Map.Entry<String, Metric> m : metrics.entrySet()) {
                System.out.println(m.getKey() + "=" + ((Gauge) m.getValue()).getValue());
            }
        }

        Thread.sleep(3000);

        for (int i = 0; i < 100; i++) {
            Map<String, Metric> metrics = set.getMetrics();
            for (Map.Entry<String, Metric> m : metrics.entrySet()) {
                System.out.println(m.getKey() + "=" + ((Gauge) m.getValue()).getValue());
            }
        }
    }
}
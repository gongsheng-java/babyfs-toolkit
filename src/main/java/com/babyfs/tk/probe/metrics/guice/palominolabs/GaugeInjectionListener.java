package com.babyfs.tk.probe.metrics.guice.palominolabs;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.spi.InjectionListener;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * An injection listener which creates a gauge for the declaring class with the given name (or the method's name, if
 * none was provided) which returns the value returned by the annotated method.
 */
public class GaugeInjectionListener<I> implements InjectionListener<I> {
    private final MetricRegistry metricRegistry;
    private final String metricName;
    private final Method method;

    public GaugeInjectionListener(MetricRegistry metricRegistry, String metricName, Method method) {
        this.metricRegistry = metricRegistry;
        this.metricName = metricName;
        this.method = method;
    }

    @Override
    public void afterInjection(final I i) {
        metricRegistry.register(metricName, new Gauge<Object>() {
            @Override
            public Object getValue() {
                return ReflectionUtils.invokeMethod(method, i);
            }
        });
    }
}

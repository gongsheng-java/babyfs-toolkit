package com.babyfs.tk.probe.metrics.guice.palominolabs;

import com.codahale.metrics.CachedGauge;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.spi.InjectionListener;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * An injection listener which creates a gauge for the declaring class with the given name (or the method's name, if
 * none was provided) which returns the value returned by the annotated method.
 */
public class CachedGaugeInjectionListener<I> implements InjectionListener<I> {
    private final MetricRegistry metricRegistry;
    private final String metricName;
    private final Method method;
    private final long timeout;
    private final TimeUnit timeoutUnit;

    public CachedGaugeInjectionListener(MetricRegistry metricRegistry, String metricName, Method method, long timeout, TimeUnit timeoutUnit) {
        this.metricRegistry = metricRegistry;
        this.metricName = metricName;
        this.method = method;
        this.timeout = timeout;
        this.timeoutUnit = timeoutUnit;
    }

    @Override
    public void afterInjection(final I i) {
        metricRegistry.register(metricName, new CachedGauge<Object>(timeout, timeoutUnit) {
            @Override
            protected Object loadValue() {
                return ReflectionUtils.invokeMethod(method, i);
            }
        });
    }
}

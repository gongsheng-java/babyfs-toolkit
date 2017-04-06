package com.babyfs.tk.probe.metrics.guice.palominolabs;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.CachedGauge;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

import java.lang.reflect.Method;

/**
 * A listener which adds gauge injection listeners to classes with gauges.
 */
public class CachedGaugeListener implements TypeListener {
    private final MetricRegistry metricRegistry;
    private final MetricNamer metricNamer;

    public CachedGaugeListener(MetricRegistry metricRegistry, MetricNamer metricNamer) {
        this.metricRegistry = metricRegistry;
        this.metricNamer = metricNamer;
    }

    @Override
    public <I> void hear(final TypeLiteral<I> literal, TypeEncounter<I> encounter) {
        Class<? super I> klass = literal.getRawType();
        for (final Method method : klass.getMethods()) {
            final CachedGauge annotation = method.getAnnotation(CachedGauge.class);
            if (annotation != null) {
                if (method.getParameterTypes().length == 0) {
                    final String metricName = metricNamer.getNameForGauge(method, annotation);
                    encounter.register(new CachedGaugeInjectionListener<I>(metricRegistry,
                            metricName,
                            method,
                            annotation.timeout(), annotation.timeoutUnit()));
                } else {
                    encounter.addError("Method %s is annotated with @Gauge but requires parameters.",
                            method);
                }
            }
        }
    }
}

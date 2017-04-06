package com.babyfs.tk.probe.metrics.guice.palominolabs;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.*;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matcher;
import com.google.inject.matcher.Matchers;

/**
 * A Guice module which instruments methods annotated with the {@link Metered}, {@link Timed}, {@link Gauge}, {@link
 * Counted}, and {@link ExceptionMetered} annotations.
 *
 * @see Gauge
 * @see Metered
 * @see Timed
 * @see ExceptionMetered
 * @see Counted
 * @see MeteredInterceptor
 * @see TimedInterceptor
 * @see GaugeInjectionListener
 */
public class MetricsInstrumentationModule extends AbstractModule {
    private final MetricRegistry metricRegistry;
    private final Matcher<? super TypeLiteral<?>> matcher;
    private final MetricNamer metricNamer;

    /**
     * @param metricRegistry The registry to use when creating meters, etc. for annotated methods.
     */
    public MetricsInstrumentationModule(MetricRegistry metricRegistry) {
        this(metricRegistry, Matchers.any());
    }

    /**
     * @param metricRegistry The registry to use when creating meters, etc. for annotated methods.
     * @param matcher        The matcher to determine which types to look for metrics in
     */
    public MetricsInstrumentationModule(MetricRegistry metricRegistry, Matcher<? super TypeLiteral<?>> matcher) {
        this(metricRegistry, matcher, new DefaultMetricNamer());
    }

    /**
     * @param metricRegistry The registry to use when creating meters, etc. for annotated methods.
     * @param matcher        The matcher to determine which types to look for metrics in
     * @param metricNamer    The metric namer to use when creating names for metrics for annotated methods
     */
    public MetricsInstrumentationModule(MetricRegistry metricRegistry, Matcher<? super TypeLiteral<?>> matcher,
                                        MetricNamer metricNamer) {
        this.metricRegistry = metricRegistry;
        this.matcher = matcher;
        this.metricNamer = metricNamer;
    }

    @Override
    protected void configure() {
        bindListener(matcher, new MeteredListener(metricRegistry, metricNamer));
        bindListener(matcher, new TimedListener(metricRegistry, metricNamer));
        bindListener(matcher, new GaugeListener(metricRegistry, metricNamer));
        bindListener(matcher, new CachedGaugeListener(metricRegistry, metricNamer));
        bindListener(matcher, new ExceptionMeteredListener(metricRegistry, metricNamer));
        bindListener(matcher, new CountedListener(metricRegistry, metricNamer));
    }
}

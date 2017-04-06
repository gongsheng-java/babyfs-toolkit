package com.babyfs.tk.probe.metrics.guice.palominolabs;

import com.codahale.metrics.annotation.*;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;

/**
 * Generates for the metrics corresponding to the various metric annotations.
 */
public interface MetricNamer {

    @Nonnull
    String getNameForCounted(@Nonnull Method method, @Nonnull Counted counted);

    @Nonnull
    String getNameForExceptionMetered(@Nonnull Method method, @Nonnull ExceptionMetered exceptionMetered);

    @Nonnull
    String getNameForGauge(@Nonnull Method method, @Nonnull Gauge gauge);

    @Nonnull
    String getNameForGauge(@Nonnull Method method, @Nonnull CachedGauge gauge);

    @Nonnull
    String getNameForMetered(@Nonnull Method method, @Nonnull Metered metered);

    @Nonnull
    String getNameForTimed(@Nonnull Method method, @Nonnull Timed timed);
}

package com.babyfs.tk.probe.metrics.guice;

import com.codahale.metrics.MetricRegistry;
import com.babyfs.tk.commons.service.ServiceModule;
import com.babyfs.tk.probe.metrics.MetricsProbe;
import com.babyfs.tk.probe.metrics.guice.palominolabs.MetricsInstrumentationModule;

/**
 * metrics的Guice集成module,{@link MetricRegistry}的实例使用{@link MetricsProbe#getRegistry()}
 */
public class MetricsModule extends ServiceModule {
    @Override
    protected void configure() {
        MetricRegistry registry = MetricsProbe.getRegistry();
        bindService(MetricRegistry.class, registry);
        MetricsInstrumentationModule instrumentationModule = new MetricsInstrumentationModule(registry);
        install(instrumentationModule);
    }
}

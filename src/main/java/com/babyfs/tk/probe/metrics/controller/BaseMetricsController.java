package com.babyfs.tk.probe.metrics.controller;

import com.babyfs.tk.probe.metrics.MetricsFormat;
import com.babyfs.tk.probe.metrics.MetricsProbe;
import com.babyfs.tk.service.biz.base.annotation.InternalAccess;
import com.babyfs.tk.service.biz.base.annotation.NoMetrics;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.net.HttpHeaders;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.*;

/**
 * 内部接口:性能监控
 */
public class BaseMetricsController {
    public static final Logger LOGGER = LoggerFactory.getLogger(BaseMetricsController.class);

    /**
     * metrics日志
     *
     * @param request
     * @param response
     */
    @RequestMapping(value = "/metrics")
    @NoMetrics
    @InternalAccess
    public void metrics(final HttpServletRequest request, final HttpServletResponse response) {
        this.metrics0(request, response, MetricsFormat.NORMAL);
    }

    /**
     * 提供给prometheus的metrics
     *
     * @param request
     * @param response
     */
    @RequestMapping(value = "/metrics_prometheus")
    @NoMetrics
    @InternalAccess
    public void metricsPrometheus(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        //oldMetrics
        //this.metrics0(request, response, MetricsFormat.PROMETHEUS);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(TextFormat.CONTENT_TYPE_004);

        Writer writer = response.getWriter();
        try {
            TextFormat.write004(writer, CollectorRegistry.defaultRegistry.filteredMetricFamilySamples(parse(request)));
            writer.flush();
        } finally {
            writer.close();
        }
    }

    private Set<String> parse(HttpServletRequest req) {
        String[] includedParam = req.getParameterValues("name[]");
        if (includedParam == null) {
            return Collections.emptySet();
        } else {
            return new HashSet<String>(Arrays.asList(includedParam));
        }
    }

    private void metrics0(final HttpServletRequest request, final HttpServletResponse response, MetricsFormat format) {
        List<String> metrics = Lists.newLinkedList();
        MetricsProbe.collectAllGauage(metrics, format);
        MetricsProbe.collectAllTimer(metrics, format);

        String result = Joiner.on("\n").join(metrics) + "\n\n";
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/plain");
        PrintWriter writer;
        try {
            writer = response.getWriter();
            writer.print(result);
            writer.flush();
        } catch (IOException e) {
            LOGGER.error("meritcs error", e);
        }
    }
}

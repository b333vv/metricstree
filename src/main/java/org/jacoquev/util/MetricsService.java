package org.jacoquev.util;

import com.intellij.openapi.project.Project;
import org.jacoquev.model.metric.value.Range;
import org.jacoquev.model.metric.value.Value;

public class MetricsService {
    private static MetricsSettings metricsSettings;

    private MetricsService() {
        // Utility class
    }

    public static void setMetricsSettings(Project project) {
        metricsSettings = MetricsUtils.get(project, MetricsSettings.class);
    }

    public static Range getRangeForMetric(String metricName) {
        MetricsSettings.MetricStub metricStub = metricsSettings.getMetrics().get(metricName);
        if (metricStub == null) {
            return Range.UNDEFINED_RANGE;
        }
        if (metricStub.isDoubleValue()) {
            return Range.of(Value.of(metricStub.getMinDoubleValue()), Value.of(metricStub.getMaxDoubleValue()));
        } else {
            return Range.of(Value.of(metricStub.getMinLongValue()), Value.of(metricStub.getMaxLongValue()));
        }
    }

}

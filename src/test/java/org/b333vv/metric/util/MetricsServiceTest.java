package org.b333vv.metric.util;

import com.intellij.openapi.project.Project;
import org.b333vv.metric.model.metric.MetricType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MetricsServiceTest {

    @Mock
    private Project mockProject;

    private MetricsService metricsService;

    // As per problem description
    private static final Set<MetricType> DOUBLE_VALUE_METRIC_TYPES = new HashSet<>(Arrays.asList(
            MetricType.TCC, MetricType.I, MetricType.A, MetricType.D, MetricType.MHF, MetricType.AHF,
            MetricType.MIF, MetricType.AIF, MetricType.CF, MetricType.PF, MetricType.LAA,
            MetricType.CDISP, MetricType.WOC, MetricType.CCC, MetricType.CCM
    ));

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Assuming MetricsService has a constructor that accepts a Project.
        // If MetricsService is typically retrieved via Project.getService(MetricsService.class),
        // this direct instantiation is a valid way to unit test the class logic itself,
        // assuming no complex lifecycle or dependency injection is missed that's critical for isLongValueMetricType.
        metricsService = new MetricsService(mockProject);
    }

    @Test
    void testIsLongValueMetricType() {
        for (MetricType metricType : MetricType.values()) {
            boolean isActuallyDoubleType = DOUBLE_VALUE_METRIC_TYPES.contains(metricType);
            boolean resultFromMethod = metricsService.isLongValueMetricType(metricType);

            if (isActuallyDoubleType) {
                assertFalse(resultFromMethod, "MetricType " + metricType + " is a double type, so isLongValueMetricType should return false.");
            } else {
                assertTrue(resultFromMethod, "MetricType " + metricType + " is not a double type, so isLongValueMetricType should return true.");
            }
        }
    }
}

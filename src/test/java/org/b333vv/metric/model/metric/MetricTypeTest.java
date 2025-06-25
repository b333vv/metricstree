package org.b333vv.metric.model.metric;

import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class MetricTypeTest {

    @Test
    void testIsLongValue() {
        Set<MetricType> doubleValueMetricTypes = Set.of(
                MetricType.TCC, MetricType.I, MetricType.A, MetricType.D,
                MetricType.MHF, MetricType.AHF, MetricType.MIF, MetricType.AIF,
                MetricType.CF, MetricType.PF, MetricType.LAA, MetricType.CDISP,
                MetricType.WOC, MetricType.CCC, MetricType.CCM);

        for (MetricType type : MetricType.values()) {
            if (doubleValueMetricTypes.contains(type)) {
                assertFalse(type.isLongValue(), "MetricType." + type.name() + " should be a double value.");
            } else {
                assertTrue(type.isLongValue(), "MetricType." + type.name() + " should be a long value.");
            }
        }
    }
}

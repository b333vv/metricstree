package org.b333vv.metricsTree.ui.tree.node;

import org.b333vv.metricsTree.model.metric.Metric;
import org.b333vv.metricsTree.util.MetricsIcons;

import javax.swing.*;

public class ClassMetricNode extends MetricNode {
    public ClassMetricNode(Metric metric) {
        super(metric);
    }

    protected Icon getIcon() {
        return MetricsIcons.CLASS_METRIC;
    }
}

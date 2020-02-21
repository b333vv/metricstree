package org.jacoquev.ui.tree.node;

import org.jacoquev.model.metric.Metric;
import org.jacoquev.util.MetricsIcons;

import javax.swing.*;

public class MethodMetricNode extends MetricNode {
    public MethodMetricNode(Metric metric) {
        super(metric);
    }

    protected Icon getIcon() {
        return MetricsIcons.METHOD_METRIC;
    }
}

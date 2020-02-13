package org.jacoquev.ui.tree.node;

import org.jacoquev.model.metric.Metric;
import org.jacoquev.util.MetricsIcons;

import javax.swing.*;

public class ClassMetricNode extends MetricNode {
    public ClassMetricNode(Metric metric) {
        super(metric);
    }

    protected Icon getIcon() {
        return MetricsIcons.CLASS_METRIC;
    }
}

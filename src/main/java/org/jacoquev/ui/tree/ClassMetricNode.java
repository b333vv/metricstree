package org.jacoquev.ui.tree;

import org.jacoquev.model.metric.Metric;
import org.jacoquev.util.MetricsIcons;

import javax.swing.*;

public class ClassMetricNode extends MetricNode {
    public ClassMetricNode(Metric metric) {
        super(metric);
    }

    protected Icon getIcon() {
//        return AllIcons.Nodes.Class;
        return MetricsIcons.CLASS_METRIC;
    }
}

package org.jacoquev.ui.tree.node;

import org.jacoquev.model.metric.Metric;
import org.jacoquev.util.MetricsIcons;

import javax.swing.*;

public class ProjectMetricNode extends MetricNode {
    public ProjectMetricNode(Metric metric) {
        super(metric);
    }

    protected Icon getIcon() {
        return MetricsIcons.PROJECT_METRIC;
    }
}

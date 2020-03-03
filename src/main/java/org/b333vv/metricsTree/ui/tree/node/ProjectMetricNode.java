package org.b333vv.metricsTree.ui.tree.node;

import org.b333vv.metricsTree.model.metric.Metric;
import org.b333vv.metricsTree.util.MetricsIcons;

import javax.swing.*;

public class ProjectMetricNode extends MetricNode {
    public ProjectMetricNode(Metric metric) {
        super(metric);
    }

    protected Icon getIcon() {
        return MetricsIcons.PROJECT_METRIC;
    }
}

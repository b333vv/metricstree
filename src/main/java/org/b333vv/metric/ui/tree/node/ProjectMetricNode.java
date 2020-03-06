package org.b333vv.metric.ui.tree.node;

import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.util.MetricsIcons;

import javax.swing.*;

public class ProjectMetricNode extends MetricNode {
    public ProjectMetricNode(Metric metric) {
        super(metric);
    }

    @Override
    protected Icon getIcon() {
        return MetricsIcons.PROJECT_METRIC;
    }
}

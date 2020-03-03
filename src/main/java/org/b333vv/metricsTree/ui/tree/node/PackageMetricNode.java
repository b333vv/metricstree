package org.b333vv.metricsTree.ui.tree.node;

import org.b333vv.metricsTree.model.metric.Metric;
import org.b333vv.metricsTree.util.MetricsIcons;

import javax.swing.*;

public class PackageMetricNode extends MetricNode {
    public PackageMetricNode(Metric metric) {
        super(metric);
    }

    protected Icon getIcon() {
        return MetricsIcons.PACKAGE_METRIC;
    }
}

package org.jacoquev.ui.tree.node;

import org.jacoquev.model.metric.Metric;
import org.jacoquev.util.MetricsIcons;

import javax.swing.*;

public class PackageMetricNode extends MetricNode {
    public PackageMetricNode(Metric metric) {
        super(metric);
    }

    protected Icon getIcon() {
        return MetricsIcons.PACKAGE_METRIC;
    }
}

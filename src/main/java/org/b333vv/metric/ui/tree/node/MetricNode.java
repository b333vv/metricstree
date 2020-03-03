package org.b333vv.metric.ui.tree.node;

import com.intellij.icons.AllIcons;
import com.intellij.ui.scale.JBUIScale;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.Sets;
import org.b333vv.metric.model.metric.value.Range;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.ui.tree.CompositeIcon;
import org.b333vv.metric.ui.tree.TreeCellRenderer;
import org.b333vv.metric.util.MetricsIcons;

import javax.swing.*;

public class MetricNode extends AbstractNode {

    protected final Metric metric;

    public MetricNode(Metric metric) {
        this.metric = metric;
    }

    protected Icon getIcon() {
        return AllIcons.Nodes.Artifact;
    }

    public Metric getMetric() {
        return metric;
    }

    @Override
    public void render(TreeCellRenderer renderer) {
        int gap = JBUIScale.isUsrHiDPI() ? 8 : 4;
        if (metric.getValue() == Value.UNDEFINED) {
            renderer.setIconToolTip("This metric was not calculated");
            renderer.setIcon(new CompositeIcon(CompositeIcon.Axis.X_AXIS, gap, getIcon(),
                    MetricsIcons.NA));
        } else if (!metric.hasAllowableValue()) {
            renderer.setIconToolTip("This metric has an unacceptable value");
            renderer.setIcon(new CompositeIcon(CompositeIcon.Axis.X_AXIS, gap, getIcon(),
                    AllIcons.General.BalloonError));
        } else {
            if (metric.getRange() == Range.UNDEFINED) {
                renderer.setIconToolTip("The desired value range is not set for this metric");
                renderer.setIcon(new CompositeIcon(CompositeIcon.Axis.X_AXIS, gap, getIcon(),
                        AllIcons.General.BalloonWarning));
            } else {
                renderer.setIconToolTip("This metric has an acceptable value");
                renderer.setIcon(new CompositeIcon(CompositeIcon.Axis.X_AXIS, gap, getIcon(),
                        AllIcons.Actions.Commit));
            }
        }
        if (Sets.inMoodMetricsSet(metric.getName())) {
            renderer.append(metric.getDescription() + " = " + metric.getValue().percentageFormat());
        } else {
            renderer.append(metric.getDescription() + " = " + metric.getFormattedValue());
        }
    }
}

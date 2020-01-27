package org.jacoquev.ui.tree;

import com.intellij.icons.AllIcons;
import com.intellij.ui.scale.JBUIScale;
import org.jacoquev.model.metric.Metric;
import org.jacoquev.model.metric.value.Range;

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
        if (!metric.hasAllowableValue()) {
            renderer.setIconToolTip("This metric has an unacceptable value");
            renderer.setIcon(new CompositeIcon(CompositeIcon.Axis.X_AXIS, gap, getIcon(),
                    AllIcons.General.BalloonError));
        } else {
            if (metric.getRange() == Range.UNDEFINED_RANGE) {
                renderer.setIconToolTip("The desired value range is not set for this metric");
                renderer.setIcon(new CompositeIcon(CompositeIcon.Axis.X_AXIS, gap, getIcon(),
                        AllIcons.General.BalloonWarning));
            } else {
                renderer.setIconToolTip("This metric has an acceptable value");
                renderer.setIcon(new CompositeIcon(CompositeIcon.Axis.X_AXIS, gap, getIcon(),
                        AllIcons.Actions.Commit));
            }
        }
        renderer.append(metric.getDescription() + " : " + metric.getFormattedValue());
    }
}

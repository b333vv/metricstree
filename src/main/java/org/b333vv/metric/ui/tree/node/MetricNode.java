/*
 * Copyright 2020 b333vv
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import org.b333vv.metric.util.MetricsService;

import javax.swing.*;

public class MetricNode extends AbstractNode {

    protected transient final Metric metric;

    public MetricNode(Metric metric) {
        this.metric = metric;
    }

    protected Icon getIcon() {
        return AllIcons.Nodes.Artifact;
    }

    protected String getText() {
        if (Sets.inMoodMetricsSet(metric.getName())) {
            return metric.getDescription() + ": " + metric.getValue().percentageFormat();
        } else {
            return metric.getDescription() + ": " + metric.getFormattedValue();
        }
    }

    public Metric getMetric() {
        return metric;
    }

    @Override
    public void render(TreeCellRenderer renderer) {
        int gap = JBUIScale.isUsrHiDPI() ? 8 : 4;
        if (MetricsService.isControlValidRanges()) {
            if (metric.getValue() == Value.UNDEFINED) {
                renderer.setIconToolTip("This metric was not calculated");
                renderer.setIcon(new CompositeIcon(CompositeIcon.Axis.X_AXIS, gap, getIcon(),
                        MetricsIcons.NA));
            } else if (!metric.hasAllowableValue()) {
                renderer.setIconToolTip("This metric has an unacceptable value");
                renderer.setIcon(new CompositeIcon(CompositeIcon.Axis.X_AXIS, gap, getIcon(),
                        MetricsIcons.INVALID_VALUE));
            } else {
                if (metric.getRange() == Range.UNDEFINED) {
                    renderer.setIconToolTip("The desired value range is not set for this metric");
                    renderer.setIcon(new CompositeIcon(CompositeIcon.Axis.X_AXIS, gap, getIcon(),
                            MetricsIcons.NOT_TRACKED));
                } else {
                    renderer.setIconToolTip("This metric has an acceptable value");
                    renderer.setIcon(new CompositeIcon(CompositeIcon.Axis.X_AXIS, gap, getIcon(),
                            MetricsIcons.VALID_VALUE));
                }
            }
        } else {
            if (metric.getValue() == Value.UNDEFINED) {
                renderer.setIconToolTip("This metric was not calculated");
                renderer.setIcon(MetricsIcons.NA);
            } else {
                renderer.setIcon(getIcon());
            }
        }
        renderer.append(getText());
    }
}
